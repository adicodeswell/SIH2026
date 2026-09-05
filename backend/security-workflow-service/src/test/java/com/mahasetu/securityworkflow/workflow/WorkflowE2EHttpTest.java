package com.mahasetu.securityworkflow.workflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mahasetu.securityworkflow.dto.WorkflowStartRequest;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.dto.OfficerDecisionRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.service.ConsentService;
import com.mahasetu.securityworkflow.client.ServiceTokenProvider;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WorkflowE2EHttpTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtDecoder jwtDecoder;
    
    @MockBean
    private ServiceTokenProvider serviceTokenProvider;

    private static WireMockServer member1MockServer;
    private static WireMockServer member2MockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        member1MockServer = new WireMockServer(0);
        member1MockServer.start();
        registry.add("mahasetu.application-service.url", member1MockServer::baseUrl);

        member2MockServer = new WireMockServer(0);
        member2MockServer.start();
        registry.add("mahasetu.interoperability-service.url", member2MockServer::baseUrl);
        registry.add("mahasetu.interoperability-service.token", () -> "MAHASETU_SUPER_SECRET_TOKEN_2026");
    }

    @BeforeEach
    void setup() {
        member1MockServer.resetAll();
        member2MockServer.resetAll();
        when(serviceTokenProvider.getAuthorizationHeader()).thenReturn("Bearer dummy-service-token");

        for (org.camunda.bpm.engine.runtime.ProcessInstance pi : processEngine.getRuntimeService().createProcessInstanceQuery().list()) {
            processEngine.getRuntimeService().deleteProcessInstance(pi.getId(), "test cleanup");
        }
    }

    @Test
    void testEndToEndWorkflow_RealHttpBoundary() throws Exception {
        // 1. Mock Keycloak JWTDecoder for Service Token
        Jwt serviceJwt = Jwt.withTokenValue("service-token")
                .header("alg", "none")
                .claim("sub", "service-client")
                .claim("realm_access", Map.of("roles", List.of("SERVICE")))
                .build();
        
        Jwt officerJwt = Jwt.withTokenValue("officer-token")
                .header("alg", "none")
                .claim("sub", "officer_123")
                .claim("realm_access", Map.of("roles", List.of("OFFICER")))
                .build();

        when(jwtDecoder.decode("service-token")).thenReturn(serviceJwt);
        when(jwtDecoder.decode("officer-token")).thenReturn(officerJwt);

        // 2. Mock Member 1 & 2 External Services
        member1MockServer.stubFor(get(urlEqualTo("/api/v1/applications/APP-E2E-123"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"applicationNumber\": \"APP-E2E-123\", \"citizenId\": \"CIT-E2E\", \"serviceCode\": \"SKILL_BENEFIT\", \"status\": \"SUBMITTED\" }")));

        member2MockServer.stubFor(get(urlEqualTo("/api/v1/interop/fetch/all/CIT-E2E"))
                .withHeader("Authorization", equalTo("Bearer MAHASETU_SUPER_SECRET_TOKEN_2026"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[ { \"citizenId\": \"CIT-E2E\", \"fullName\": \"Test Citizen\", \"highestDegree\": \"B.Tech\" } ]")));

        member1MockServer.stubFor(post(urlEqualTo("/internal/v1/applications/APP-E2E-123/workflow-status"))
                .willReturn(aResponse().withStatus(200)));

        // 3. Setup Consent
        ConsentRequest consentReq = new ConsentRequest();
        consentReq.setDataScope("education,employment,skills");
        consentReq.setPurpose("verification");
        consentReq.setRequestingDepartmentId("DEPT-1");
        consentService.grantConsent("CIT-E2E", consentReq);

        // 4. Start Workflow via HTTP
        WorkflowStartRequest startReq = new WorkflowStartRequest();
        startReq.setApplicationId("APP-E2E-123");
        startReq.setWorkflowKey("application-orchestration");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer service-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> startRes = restTemplate.exchange("/internal/v1/workflows", HttpMethod.POST, new HttpEntity<>(startReq, headers), String.class);
        assertEquals(HttpStatus.OK, startRes.getStatusCode(), "Workflow start should return 200 OK");

        String processInstanceId = new ObjectMapper().readTree(startRes.getBody()).get("processInstanceId").asText();
        assertNotNull(processInstanceId, "Process instance id should not be null");

        // Wait for Camunda engine to reach UserTask
        Thread.sleep(1500);

        // 5. Verify Task created for this specific process instance
        List<Task> tasks = processEngine.getTaskService().createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey("UserTask_OfficerReview")
                .taskCandidateGroup("OFFICER")
                .list();
        assertEquals(1, tasks.size(), "Should pause at Officer Review task");
        String taskId = tasks.get(0).getId();

        // 6. Claim Task via HTTP
        HttpHeaders officerHeaders = new HttpHeaders();
        officerHeaders.set("Authorization", "Bearer officer-token");
        
        ResponseEntity<String> claimRes = restTemplate.exchange("/api/v1/officer/reviews/" + taskId + "/claim", HttpMethod.POST, new HttpEntity<>(null, officerHeaders), String.class);
        assertEquals(HttpStatus.OK, claimRes.getStatusCode(), "Task claim should return 200 OK");

        // 7. Approve Task via HTTP
        OfficerDecisionRequest decisionReq = new OfficerDecisionRequest();
        decisionReq.setDecision("APPROVE");
        
        ResponseEntity<String> decisionRes = restTemplate.exchange("/api/v1/officer/reviews/" + taskId + "/decision", HttpMethod.POST, new HttpEntity<>(decisionReq, officerHeaders), String.class);
        assertEquals(HttpStatus.OK, decisionRes.getStatusCode(), "Task approval should return 200 OK");

        // Wait for callback propagation
        Thread.sleep(1000);

        // 8. Verify Callbacks
        member1MockServer.verify(1, postRequestedFor(urlEqualTo("/internal/v1/applications/APP-E2E-123/workflow-status"))
                .withRequestBody(matchingJsonPath("$.status", equalTo("PENDING_OFFICER_REVIEW"))));
        
        member1MockServer.verify(1, postRequestedFor(urlEqualTo("/internal/v1/applications/APP-E2E-123/workflow-status"))
                .withRequestBody(matchingJsonPath("$.status", equalTo("APPROVED")))
                .withRequestBody(matchingJsonPath("$.officerId", equalTo("officer_123"))));
    }
}
