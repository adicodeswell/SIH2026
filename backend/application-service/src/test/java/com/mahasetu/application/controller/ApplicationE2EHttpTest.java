package com.mahasetu.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mahasetu.application.dto.CreateApplicationRequest;
import com.mahasetu.application.dto.WorkflowStatusCallbackRequest;
import com.mahasetu.application.entity.ApplicationStatus;
import com.mahasetu.application.integration.ServiceTokenProvider;
import com.mahasetu.application.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Application Service HTTP boundary test.
 *
 * This test verifies REAL HTTP communication between:
 * - Test client -> Application Service (via TestRestTemplate)
 * - Application Service -> Workflow Service (via WireMock standing in for Security-Workflow-Service)
 * - Test client simulating Workflow callback -> Application Service internal endpoint
 *
 * It does NOT test the full platform end-to-end (no real Camunda, no real Interoperability Service).
 * It proves the HTTP boundary contracts are correct.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationE2EHttpTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ServiceTokenProvider serviceTokenProvider;

    private static WireMockServer workflowMockServer;

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationEventRepository applicationEventRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        workflowMockServer = new WireMockServer(0);
        workflowMockServer.start();
        registry.add("integration.workflow-service.url", workflowMockServer::baseUrl);
    }

    @BeforeEach
    void setup() {
        workflowMockServer.resetAll();
        when(serviceTokenProvider.getAuthorizationHeader()).thenReturn("Bearer dummy-service-token");

        // Clean up in correct order (respect FK constraints)
        applicationEventRepository.deleteAll();
        applicationRepository.deleteAll();
        serviceRepository.deleteAll();
        departmentRepository.deleteAll();
        citizenRepository.deleteAll();

        // Seed test data
        com.mahasetu.application.entity.Citizen citizen = new com.mahasetu.application.entity.Citizen();
        citizen.setCitizenId("MH1001");
        citizen.setName("Test Citizen");
        citizen.setMobile("9999999999");
        citizen.setEmail("test@mahasetu.gov.in");
        citizen.setDateOfBirth(LocalDate.of(1990, 1, 1));
        citizenRepository.save(citizen);

        com.mahasetu.application.entity.Department department = new com.mahasetu.application.entity.Department();
        department.setDepartmentCode("DEPT_1");
        department.setName("Test Department");
        department.setStatus("ACTIVE");
        departmentRepository.save(department);

        com.mahasetu.application.entity.Service service = new com.mahasetu.application.entity.Service();
        service.setServiceCode("SKILL_BENEFIT");
        service.setServiceName("Skill Benefit");
        service.setActive(true);
        service.setDepartment(department);
        serviceRepository.save(service);
    }

    @AfterEach
    void tearDown() {
        if (workflowMockServer != null && workflowMockServer.isRunning()) {
            workflowMockServer.resetAll();
        }
    }

    @Test
    void testEndToEndApplicationCreationAndCallback_RealHttpBoundary() throws Exception {
        // 1. Mock JwtDecoder for citizen and service tokens
        Jwt citizenJwt = Jwt.withTokenValue("citizen-token")
                .header("alg", "none")
                .claim("sub", "MH1001")
                .claim("realm_access", Map.of("roles", List.of("CITIZEN")))
                .build();

        Jwt serviceJwt = Jwt.withTokenValue("service-token")
                .header("alg", "none")
                .claim("sub", "service-client")
                .claim("realm_access", Map.of("roles", List.of("SERVICE")))
                .build();

        when(jwtDecoder.decode("citizen-token")).thenReturn(citizenJwt);
        when(jwtDecoder.decode("service-token")).thenReturn(serviceJwt);

        // 2. Stub WireMock to accept the workflow-start request
        workflowMockServer.stubFor(post(urlEqualTo("/internal/v1/workflows"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"processInstanceId\":\"test-proc-1\"}")));

        // 3. Create Application via real HTTP POST
        CreateApplicationRequest createReq = new CreateApplicationRequest();
        createReq.setCitizenId("MH1001");
        createReq.setServiceCode("SKILL_BENEFIT");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer citizen-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> createRes = restTemplate.exchange(
                "/api/v1/applications", HttpMethod.POST,
                new HttpEntity<>(createReq, headers), String.class);

        assertEquals(HttpStatus.CREATED, createRes.getStatusCode(),
                "Application creation should return 201 Created. Body: " + createRes.getBody());

        String appNumber = new ObjectMapper().readTree(createRes.getBody()).get("applicationNumber").asText();
        assertNotNull(appNumber, "Application number should not be null");

        // 4. Verify Application Service sent the workflow-start HTTP request to WireMock
        workflowMockServer.verify(1, postRequestedFor(urlEqualTo("/internal/v1/workflows"))
                .withRequestBody(matchingJsonPath("$.applicationId", equalTo(appNumber)))
                .withRequestBody(matchingJsonPath("$.workflowKey", equalTo("application-orchestration")))
                .withHeader("Authorization", equalTo("Bearer dummy-service-token")));

        // 5. Simulate Workflow Status Callback (workflow service -> application service)
        WorkflowStatusCallbackRequest callbackReq = new WorkflowStatusCallbackRequest();
        callbackReq.setApplicationId(appNumber);
        callbackReq.setProcessInstanceId("camunda-proc-1");
        callbackReq.setStatus("PENDING_OFFICER_REVIEW");

        HttpHeaders serviceHeaders = new HttpHeaders();
        serviceHeaders.set("Authorization", "Bearer service-token");
        serviceHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> callbackRes = restTemplate.exchange(
                "/internal/v1/applications/" + appNumber + "/workflow-status",
                HttpMethod.POST,
                new HttpEntity<>(callbackReq, serviceHeaders),
                String.class);

        assertEquals(HttpStatus.OK, callbackRes.getStatusCode(),
                "Callback should return 200 OK. Body: " + callbackRes.getBody());
        assertEquals("PENDING_OFFICER_REVIEW",
                new ObjectMapper().readTree(callbackRes.getBody()).get("status").asText(),
                "Application status should be PENDING_OFFICER_REVIEW after callback");
    }
}
