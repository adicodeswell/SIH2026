package com.mahasetu.securityworkflow.workflow;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.service.ConsentService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApplicationOrchestrationWorkflowTest {

    private static WireMockServer member1MockServer;
    private static WireMockServer member2MockServer;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ConsentService consentService;

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
    }

    @Test
    void testWorkflow_WithValidConsent_ShouldFetchInteropDataAndCallbackSuccess() {
        // 1. Stub Member 1 Application Service GET application
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/" + APP_ID))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "applicationNumber": "APP-12345",
                                  "status": "SUBMITTED",
                                  "citizenId": "CIT-99999",
                                  "serviceCode": "SRV-EDU"
                                }
                                """)));

        // 2. Stub Member 1 status callback endpoint
        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + APP_ID + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // 3. Setup Consent matching SRV-EDU policy (dataScope: education, purpose: verification)
        ConsentRequest request = new ConsentRequest();
        request.setDataScope("education");
        request.setPurpose("verification");
        request.setRequestingDepartmentId("DEPT-1");
        consentService.grantConsent(CITIZEN_ID, request);

        // 4. Stub Member 2 Interoperability Service
        member2MockServer.stubFor(get(urlEqualTo("/api/v1/interop/fetch/all/" + CITIZEN_ID))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "citizenId": "CIT-99999",
                                    "fullName": "Test Citizen",
                                    "highestDegree": "B.Tech"
                                  }
                                ]
                                """)));

        // 5. Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", APP_ID));

        // 6. Verify process execution path
        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "Task_FetchInterop", "Task_CallbackSuccess", "EndEvent_Success");

        // 7. Verify process variables
        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
        assertEquals("SUCCESS", workflowStatus);

        Object interopResult = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("interoperabilityResult")
                .singleResult()
                .getValue();
        assertNotNull(interopResult);

        // 8. Verify callback was dispatched to Member 1
        member1MockServer.verify(postRequestedFor(urlEqualTo("/internal/v1/applications/" + APP_ID + "/workflow-status"))
                .withRequestBody(containing("\"status\":\"SUCCESS\"")));
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
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "Task_FetchInterop", "BoundaryEvent_Interop", "Task_HandleFailure", "Task_CallbackFailure", "EndEvent_Failed");

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
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "BoundaryEvent_Consent", "Task_HandleFailure", "Task_CallbackFailure", "EndEvent_Failed");
        assertThat(processInstance).hasNotPassed("Task_FetchInterop");

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
