import re

file = 'backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationE2EHttpTest.java'
with open(file, 'r') as f:
    content = f.read()

submit_code = """
        // 3.5 Submit Application
        ResponseEntity<String> submitRes = restTemplate.exchange(
                "/api/v1/applications/" + appNumber + "/submit", HttpMethod.POST,
                new HttpEntity<>(null, headers), String.class);
        assertEquals(HttpStatus.OK, submitRes.getStatusCode(),
                "Application submit should return 200 OK.");
"""

content = content.replace('// 4. Verify Application Service sent the workflow-start HTTP request to WireMock', submit_code + '\n        // 4. Verify Application Service sent the workflow-start HTTP request to WireMock')

# ConsentClient mock for submitApplication
consent_mock = """
        when(jwtDecoder.decode("citizen-token")).thenReturn(citizenJwt);
        when(jwtDecoder.decode("service-token")).thenReturn(serviceJwt);

        workflowMockServer.stubFor(com.github.tomakehurst.client.WireMock.get(com.github.tomakehurst.client.WireMock.urlMatching("/internal/v1/consents/application/.*"))
                .willReturn(com.github.tomakehurst.client.WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\\"hasConsent\\":true}")));
"""
content = content.replace('when(jwtDecoder.decode("citizen-token")).thenReturn(citizenJwt);\n        when(jwtDecoder.decode("service-token")).thenReturn(serviceJwt);', consent_mock)

with open(file, 'w') as f:
    f.write(content)

