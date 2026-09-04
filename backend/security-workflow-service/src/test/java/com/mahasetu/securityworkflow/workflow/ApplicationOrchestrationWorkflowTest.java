package com.mahasetu.securityworkflow.workflow;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.dto.OfficerDecisionResponse;
import com.mahasetu.securityworkflow.entity.AuditLog;
import com.mahasetu.securityworkflow.service.AuditService;
import com.mahasetu.securityworkflow.service.ConsentService;
import com.mahasetu.securityworkflow.service.OfficerTaskService;
import com.mahasetu.securityworkflow.client.ServiceTokenProvider;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ApplicationOrchestrationWorkflowTest {

    private static WireMockServer member1MockServer;
    private static WireMockServer member2MockServer;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private OfficerTaskService officerTaskService;

    @Autowired
    private AuditService auditService;

    @MockBean
    private ServiceTokenProvider serviceTokenProvider;

    private static final String APP_ID = "APP-12345";
    private static final String CITIZEN_ID = "CIT-99999";
    private static final String TOKEN = "MAHASETU_SUPER_SECRET_TOKEN_2026";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("mahasetu.application-service.url", member1MockServer::baseUrl);
        registry.add("mahasetu.interoperability-service.url", member2MockServer::baseUrl);
        registry.add("mahasetu.interoperability-service.token", () -> TOKEN);
    }

    @BeforeAll
    static void startMockServers() {
        member1MockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        member1MockServer.start();

        member2MockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        member2MockServer.start();
    }

    @AfterAll
    static void stopMockServers() {
        member1MockServer.stop();
        member2MockServer.stop();
    }

    @BeforeEach
    void setup() {
        member1MockServer.resetAll();
        member2MockServer.resetAll();
        when(serviceTokenProvider.getAuthorizationHeader()).thenReturn("Bearer service-token");
    }

    private void setupApplicationAndInteropMocks(String appId, String citizenId) {
        // Stub Member 1 Application Service GET application
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/" + appId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "applicationNumber": "%s",
                                  "status": "SUBMITTED",
                                  "citizenId": "%s",
                                  "serviceCode": "SRV-EDU"
                                }
                                """.formatted(appId, citizenId))));

        // Stub Member 1 status callback endpoint
        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // Setup Consent matching SRV-EDU policy (dataScope: education, purpose: verification)
        ConsentRequest request = new ConsentRequest();
        request.setDataScope("education");
        request.setPurpose("verification");
        request.setRequestingDepartmentId("DEPT-1");
        consentService.grantConsent(citizenId, request);

        // Stub Member 2 Interoperability Service
        member2MockServer.stubFor(get(urlEqualTo("/api/v1/interop/fetch/all/" + citizenId))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "citizenId": "%s",
                                    "fullName": "Test Citizen",
                                    "highestDegree": "B.Tech"
                                  }
                                ]
                                """.formatted(citizenId))));
    }

    @Test
    void testWorkflow_WithValidConsent_ShouldPauseAtOfficerReviewAndCallbackPending() {
        setupApplicationAndInteropMocks(APP_ID, CITIZEN_ID);

        // Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", APP_ID));

        // Verify process execution path has reached the User Task and genuinely paused
        assertThat(processInstance).isNotEnded();
        assertThat(processInstance).isWaitingAt("UserTask_OfficerReview");
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "Task_FetchInterop",
                "Task_SetPendingReview", "Task_CallbackPendingReview");

        // Verify intermediate workflow status
        Object workflowStatus = processEngine.getRuntimeService().getVariable(processInstance.getId(), "workflowStatus");
        assertEquals("PENDING_OFFICER_REVIEW", workflowStatus);

        // Verify callback was dispatched to Member 1 with PENDING_OFFICER_REVIEW
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + APP_ID + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"PENDING_OFFICER_REVIEW\"")));

        // Verify Camunda task exists for candidateGroup OFFICER
        Task officerTask = processEngine.getTaskService().createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey("UserTask_OfficerReview")
                .singleResult();
        assertNotNull(officerTask);
        assertEquals("Officer Review", officerTask.getName());
    }

    @Test
    void testWorkflow_OfficerApprove_ResumesWorkflowAndCallbackApproved() {
        String appId = "APP-APPROVE-1";
        String citizenId = "CIT-APPROVE-1";
        setupApplicationAndInteropMocks(appId, citizenId);

        // Start Workflow -> reaches pause
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", appId));

        assertThat(processInstance).isWaitingAt("UserTask_OfficerReview");

        Task officerTask = processEngine.getTaskService().createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey("UserTask_OfficerReview")
                .singleResult();
        assertNotNull(officerTask);

        // Complete decision via OfficerTaskService
        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(
                officerTask.getId(),
                "officer_verma",
                "APPROVE",
                "Qualifications verified successfully"
        );
        assertEquals("APPROVE", response.getDecision());
        assertEquals("COMPLETED", response.getStatus());

        // Assert process is now completed
        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Gateway_OfficerDecision", "Task_SetApproved", "Task_CallbackApproved", "EndEvent_Approved");
        assertThat(processInstance).hasNotPassed("Task_SetRejected");

        // Verify final workflow status
        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
        assertEquals("APPROVED", workflowStatus);

        // Verify APPROVED callback sent to Member 1
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"APPROVED\""))
                .withRequestBody(containing("\"officerId\":\"officer_verma\"")));

        // Verify immutable audit log record created
        List<AuditLog> auditLogs = auditService.getAuditLogsForApplication(appId);
        assertFalse(auditLogs.isEmpty());
        AuditLog log = auditLogs.get(0);
        assertEquals("officer_verma", log.getActorId());
        assertEquals("APPROVE", log.getPurpose());
        assertEquals("APPLICATION", log.getResourceType());
        assertTrue(log.getMetadata().contains("Qualifications verified successfully"));
    }

    @Test
    void testWorkflow_OfficerReject_ResumesWorkflowAndCallbackRejected() {
        String appId = "APP-REJECT-1";
        String citizenId = "CIT-REJECT-1";
        setupApplicationAndInteropMocks(appId, citizenId);

        // Start Workflow -> reaches pause
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", appId));

        assertThat(processInstance).isWaitingAt("UserTask_OfficerReview");

        Task officerTask = processEngine.getTaskService().createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey("UserTask_OfficerReview")
                .singleResult();
        assertNotNull(officerTask);

        // Complete decision via OfficerTaskService with REJECT
        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(
                officerTask.getId(),
                "officer_kulkarni",
                "REJECT",
                "Degree certificate mismatch"
        );
        assertEquals("REJECT", response.getDecision());
        assertEquals("COMPLETED", response.getStatus());

        // Assert process is now completed at Rejected end event
        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Gateway_OfficerDecision", "Task_SetRejected", "Task_CallbackRejected", "EndEvent_Rejected");
        assertThat(processInstance).hasNotPassed("Task_SetApproved");

        // Verify final workflow status
        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
        assertEquals("REJECTED", workflowStatus);

        // Verify REJECTED callback sent to Member 1 with reason and officerId
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"REJECTED\""))
                .withRequestBody(containing("\"officerId\":\"officer_kulkarni\""))
                .withRequestBody(containing("\"failureReason\":\"Degree certificate mismatch\"")));

        // Verify immutable audit log record created
        List<AuditLog> auditLogs = auditService.getAuditLogsForApplication(appId);
        assertFalse(auditLogs.isEmpty());
        AuditLog log = auditLogs.get(0);
        assertEquals("officer_kulkarni", log.getActorId());
        assertEquals("REJECT", log.getPurpose());
        assertTrue(log.getMetadata().contains("Degree certificate mismatch"));
    }

    @Test
    void testWorkflow_WithDeniedConsent_ShouldNotCallMember2AndCallbackDenied() {
        String appId = "APP-NO-CONSENT";

        // Stub Member 1 Application Service
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/" + appId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "applicationNumber": "APP-NO-CONSENT",
                                  "status": "SUBMITTED",
                                  "citizenId": "CIT-88888",
                                  "serviceCode": "SRV-EDU"
                                }
                                """)));

        // Stub Member 1 status callback endpoint
        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // Intentionally NOT granting consent for CIT-88888

        // Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", appId));

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "Task_SetConsentDenied", "Task_CallbackDenied", "EndEvent_Denied");
        assertThat(processInstance).hasNotPassed("Task_FetchInterop");
        assertThat(processInstance).hasNotPassed("UserTask_OfficerReview");

        // Verify Member 2 was NEVER called
        member2MockServer.verify(0, getRequestedFor(urlPathMatching("/api/v1/interop/.*")));

        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
        assertEquals("CONSENT_DENIED", workflowStatus);

        // Verify callback was dispatched to Member 1 with CONSENT_DENIED
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"CONSENT_DENIED\"")));
    }

    @Test
    void testWorkflow_WhenApplicationServiceFails_ShouldHandleFailureAndCallback() {
        String appId = "APP-FETCH-FAIL";

        // Stub Member 1 to return 500 error
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/" + appId))
                .willReturn(aResponse().withStatus(500)));

        // Stub Member 1 callback endpoint
        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", appId));

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Task_InitializeApp", "BoundaryEvent_InitApp", "Task_HandleFailure", "Task_CallbackFailure", "EndEvent_Failed");
        assertThat(processInstance).hasNotPassed("Task_VerifyConsent");
        assertThat(processInstance).hasNotPassed("Task_FetchInterop");
        assertThat(processInstance).hasNotPassed("UserTask_OfficerReview");

        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
        assertEquals("FAILED", workflowStatus);

        // Verify callback was dispatched to Member 1 with FAILED status
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"FAILED\"")));
    }

    @Test
    void testWorkflow_WhenInteropServiceFails_ShouldHandleFailureAndCallback() {
        String appId = "APP-INTEROP-FAIL";
        String citizenId = "CIT-INTEROP-FAIL";

        // Stub Member 1
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/" + appId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "applicationNumber": "APP-INTEROP-FAIL",
                                  "status": "SUBMITTED",
                                  "citizenId": "CIT-INTEROP-FAIL",
                                  "serviceCode": "SRV-EDU"
                                }
                                """)));

        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // Grant consent
        ConsentRequest request = new ConsentRequest();
        request.setDataScope("education");
        request.setPurpose("verification");
        request.setRequestingDepartmentId("DEPT-1");
        consentService.grantConsent(citizenId, request);

        // Stub Member 2 to fail with 500 error
        member2MockServer.stubFor(get(urlEqualTo("/api/v1/interop/fetch/all/" + citizenId))
                .willReturn(aResponse().withStatus(500)));

        // Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", appId));

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "Task_FetchInterop",
                "BoundaryEvent_Interop", "Task_HandleFailure", "Task_CallbackFailure", "EndEvent_Failed");
        assertThat(processInstance).hasNotPassed("UserTask_OfficerReview");

        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
        assertEquals("FAILED", workflowStatus);

        // Verify callback was dispatched to Member 1 with FAILED status
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"FAILED\"")));
    }

    @Test
    void testWorkflow_WhenServiceCodeUnsupported_ShouldHandleFailureAndCallback() {
        String appId = "APP-UNSUPPORTED-CODE";
        String citizenId = "CIT-UNSUPPORTED";

        // Stub Member 1 with unconfigured serviceCode
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/" + appId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "applicationNumber": "APP-UNSUPPORTED-CODE",
                                  "status": "SUBMITTED",
                                  "citizenId": "CIT-UNSUPPORTED",
                                  "serviceCode": "UNKNOWN_SERVICE_XYZ"
                                }
                                """)));

        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", appId));

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent",
                "BoundaryEvent_Consent", "Task_HandleFailure", "Task_CallbackFailure", "EndEvent_Failed");
        assertThat(processInstance).hasNotPassed("Task_FetchInterop");
        assertThat(processInstance).hasNotPassed("UserTask_OfficerReview");

        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
        assertEquals("FAILED", workflowStatus);

        // Verify callback was dispatched to Member 1 with FAILED status
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"FAILED\"")));
    }
}
