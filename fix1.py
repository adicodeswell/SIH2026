import re

def run():
    with open('backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/controller/OfficerReviewController.java', 'r') as f:
        content = f.read()

    old_extract = """    private String extractUserId(Authentication authentication) {
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String username = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (username != null) return username;
        }
        return authentication.getName();
    }"""

    new_extract = """    private String extractUserId(Authentication authentication) {
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String username = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (username != null && !username.trim().isEmpty()) {
                return username.trim();
            }
        }
        String name = authentication.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Officer identity not found");
        }
        return name.trim();
    }"""

    if old_extract in content:
        content = content.replace(old_extract, new_extract)
        with open('backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/controller/OfficerReviewController.java', 'w') as f:
            f.write(content)
        print("Fix 1 applied successfully")
    else:
        print("Could not find the target code for Fix 1")

run()
