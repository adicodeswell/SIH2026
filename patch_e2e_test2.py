import re

with open('./backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationE2EHttpTest.java', 'r') as f:
    content = f.read()

old_code = """        // Act - Submit Application
        mockMvc.perform(post("/api/v1/applications/MH-2026-DE4375/submit")
                .header("Authorization", "Bearer " + testCitizenToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());"""
                
new_code = """        String responseContent = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseContent);
        String generatedAppId = rootNode.get("applicationNumber").asText();

        // Act - Submit Application
        mockMvc.perform(post("/api/v1/applications/" + generatedAppId + "/submit")
                .header("Authorization", "Bearer " + testCitizenToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());"""

content = content.replace(old_code, new_code)
# Also fix the verify to not hardcode applicationId
old_verify_body = """.withRequestBody(matchingJsonPath("$.applicationId", equalTo("MH-2026-DE4375")))"""
new_verify_body = """.withRequestBody(matchingJsonPath("$.applicationId", matching("MH-.*")))"""

content = re.sub(r'\.withRequestBody\(matchingJsonPath\("\$\.applicationId", equalTo\("MH-.*"\)\)\)', new_verify_body, content)


with open('./backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationE2EHttpTest.java', 'w') as f:
    f.write(content)
