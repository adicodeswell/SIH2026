import re

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'r') as f:
    content = f.read()

submit_method = """
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationResponse submitApplication(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication, org.springframework.beans.factory.annotation.Autowired com.mahasetu.application.integration.ConsentClient consentClient) {
        String citizenId = authentication.getName();
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String claim = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (claim != null) citizenId = claim;
        }
        return applicationService.submitApplication(id, citizenId.toUpperCase(), consentClient);
    }
"""

if "submitApplication(" not in content:
    content = content.replace("public ApplicationResponse updateApplicationStatus", submit_method + "\n    public ApplicationResponse updateApplicationStatus")

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'w') as f:
    f.write(content)
