package com.mahasetu.securityworkflow.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mahasetu.securityworkflow.client.ServiceTokenProvider;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class WorkflowIdempotencyTest {

    private static WireMockServer member1MockServer;
    private static WireMockServer member2MockServer;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ConsentService consentService;

    @MockBean
    private ServiceTokenProvider serviceTokenProvider;

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

    private void setupMocksForApp(String appId, String citizenId) {
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

        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        ConsentRequest request = new ConsentRequest();
        request.setDataScope("education");
        request.setPurpose("verification");
        request.setRequestingDepartmentId("DEPT-1");
        consentService.grantConsent(citizenId, request);

        member2MockServer.stubFor(get(urlEqualTo("/api/v1/interop/fetch/all/" + citizenId))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "citizenId": "%s",
                                    "fullName": "Test Citizen"
                                  }
                                ]
                                """.formatted(citizenId))));
    }

    @Test
    void testScenarioA_NoActiveProcess_StartsExactlyOneProcess() {
        String appId = "APP-IDEM-A";
        String citizenId = "CIT-IDEM-A";
        setupMocksForApp(appId, citizenId);

        String processInstanceId = workflowService.startWorkflow(appId, "application-orchestration");

        assertNotNull(processInstanceId);

        // Verify exactly 1 active process exists for this business key
        long count = processEngine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey("application-orchestration")
                .processInstanceBusinessKey(appId)
                .active()
                .count();
        assertEquals(1, count);
    }

    @Test
    void testScenarioB_ActiveProcessExists_ReturnsExistingIdWithoutCreatingNew() {
        String appId = "APP-IDEM-B";
        String citizenId = "CIT-IDEM-B";
        setupMocksForApp(appId, citizenId);

        // First call
        String firstId = workflowService.startWorkflow(appId, "application-orchestration");

        // Second call with same applicationId and workflowKey
        String secondId = workflowService.startWorkflow(appId, "application-orchestration");

        // Should return the exact same instance ID
        assertEquals(firstId, secondId);

        // Verify only 1 process instance exists total
        long count = processEngine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey("application-orchestration")
                .processInstanceBusinessKey(appId)
                .count();
        assertEquals(1, count);
    }

    @Test
    void testScenarioC_CompletedProcess_AllowsNewWorkflow() {
        String appId = "APP-IDEM-C";

        // Stub app to fail so the process ends immediately
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/" + appId))
                .willReturn(aResponse().withStatus(500)));
        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/" + appId + "/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // First run completes immediately (ends at EndEvent_Failed)
        String firstId = workflowService.startWorkflow(appId, "application-orchestration");

        // Verify first process is completed/ended (no active process)
        long activeCount = processEngine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey("application-orchestration")
                .processInstanceBusinessKey(appId)
                .active()
                .count();
        assertEquals(0, activeCount);

        // Second run starting workflow for completed application
        String secondId = workflowService.startWorkflow(appId, "application-orchestration");

        assertNotEquals(firstId, secondId);
    }

    @Test
    void testScenarioD_DuplicateStartCalls_DoNotCreateTwoActiveWorkflows() {
        String appId = "APP-IDEM-D";
        String citizenId = "CIT-IDEM-D";
        setupMocksForApp(appId, citizenId);

        String id1 = workflowService.startWorkflow(appId, "application-orchestration");
        String id2 = workflowService.startWorkflow(appId, "application-orchestration");
        String id3 = workflowService.startWorkflow(appId, "application-orchestration");

        assertEquals(id1, id2);
        assertEquals(id2, id3);

        long activeCount = processEngine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey("application-orchestration")
                .processInstanceBusinessKey(appId)
                .active()
                .count();
        assertEquals(1, activeCount);
    }
}
