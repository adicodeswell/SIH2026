import re

def run():
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/controller/OfficerReviewControllerSecurityTest.java', 'r') as f:
        content = f.read()

    new_tests = """
    @Test
    void testSubmitDecision_ValidPreferredUsername_ExtractsCorrectly() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "APPROVE", "officer123", null, LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), eq("officer123"), eq("SKILLS"), eq("APPROVE"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))
                        .jwt(j -> j.claim("department", "SKILLS").claim("preferred_username", "officer123")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\\"decision\\": \\"APPROVE\\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitDecision_EmptyPreferredUsernameValidFallback_ExtractsCorrectly() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "APPROVE", "fallbackUser", null, LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), eq("fallbackUser"), eq("SKILLS"), eq("APPROVE"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))
                        .jwt(j -> j.claim("department", "SKILLS")
                                .claim("preferred_username", "")
                                .subject("fallbackUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\\"decision\\": \\"APPROVE\\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitDecision_WhitespacePreferredUsernameValidFallback_ExtractsCorrectly() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "APPROVE", "fallbackUser", null, LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), eq("fallbackUser"), eq("SKILLS"), eq("APPROVE"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))
                        .jwt(j -> j.claim("department", "SKILLS")
                                .claim("preferred_username", "   ")
                                .subject("fallbackUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\\"decision\\": \\"APPROVE\\"}"))
                .andExpect(status().isOk());
    }
"""

    if "testSubmitDecision_ValidPreferredUsername_ExtractsCorrectly" not in content:
        content = content.replace('    @Test\n    void testSubmitDecision_OfficerRole_Allowed()', new_tests + '\n    @Test\n    void testSubmitDecision_OfficerRole_Allowed()')
        with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/controller/OfficerReviewControllerSecurityTest.java', 'w') as f:
            f.write(content)
        print("Fix 1 tests applied successfully")
    else:
        print("Tests already exist")

run()
