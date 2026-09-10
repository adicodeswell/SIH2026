import re

with open('./backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationE2EHttpTest.java', 'r') as f:
    content = f.read()

# We need to stub ConsentClient endpoint for mock server, and call submit
old_call = """        // Act - Create Application
        MvcResult result = mockMvc.perform(post("/api/v1/applications")"""

new_call = """        // Mock consent check
        mockServer.stubFor(get(urlPathEqualTo("/internal/v1/consents/check"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("true")));

        // Act - Create Application
        MvcResult result = mockMvc.perform(post("/api/v1/applications")"""

content = content.replace(old_call, new_call)

old_verify = """        // Verify Workflow was called via Wiremock
        mockServer.verify(1, postRequestedFor(urlEqualTo("/internal/v1/workflows"))"""

new_verify = """        // Act - Submit Application
        mockMvc.perform(post("/api/v1/applications/MH-2026-DE4375/submit")
                .header("Authorization", "Bearer " + testCitizenToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify Workflow was called via Wiremock
        mockServer.verify(1, postRequestedFor(urlEqualTo("/internal/v1/workflows"))"""
        
content = content.replace(old_verify, new_verify)

with open('./backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationE2EHttpTest.java', 'w') as f:
    f.write(content)
