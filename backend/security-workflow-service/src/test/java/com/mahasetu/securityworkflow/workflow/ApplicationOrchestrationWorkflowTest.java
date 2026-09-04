package com.mahasetu.securityworkflow.workflow;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
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

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

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
    void testWorkflow_WithValidConsent_ShouldFetchInteropData() {
        // Stub Member 1 Application Service
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

        // Setup Consent
        ConsentRequest request = new ConsentRequest();
        request.setDataScope("education,employment,skills");
        request.setPurpose("verification");
        request.setRequestingDepartmentId("DEPT-1");
        consentService.grantConsent(CITIZEN_ID, request);

        // Stub Member 2 Interoperability Service
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

        // Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", APP_ID));

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "Task_FetchInterop", "EndEvent_Success");
        
        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
                
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", workflowStatus);
        
        Object interopResult = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("interoperabilityResult")
                .singleResult()
                .getValue();
                
        org.junit.jupiter.api.Assertions.assertNotNull(interopResult);
    }

    @Test
    void testWorkflow_WithDeniedConsent_ShouldNotCallMember2() {
        // Stub Member 1 Application Service
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/APP-NO-CONSENT"))
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

        // Intentionally NOT granting consent for CIT-88888

        // Start Workflow
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey("application-orchestration", Map.of("applicationId", "APP-NO-CONSENT"));

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassed("Task_InitializeApp", "Task_VerifyConsent", "Task_SetConsentDenied", "EndEvent_Denied");
        assertThat(processInstance).hasNotPassed("Task_FetchInterop");

        // Verify Member 2 was NEVER called
        member2MockServer.verify(0, getRequestedFor(urlPathMatching("/api/v1/interop/.*")));
        
        Object workflowStatus = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("workflowStatus")
                .singleResult()
                .getValue();
                
        org.junit.jupiter.api.Assertions.assertEquals("CONSENT_DENIED", workflowStatus);
    }
}
