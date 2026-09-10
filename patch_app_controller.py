with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'r') as f:
    content = f.read()

new_method = """    @GetMapping("/me")
    @PreAuthorize("hasRole('CITIZEN')")
    public List<com.mahasetu.application.dto.CitizenApplicationSummaryResponse> getMyApplications(org.springframework.security.core.Authentication authentication) {
        String citizenId = authentication.getName();
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String claim = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (claim != null) citizenId = claim;
        }
        return applicationService.getApplicationsForCitizen(citizenId.toUpperCase());
    }

    @GetMapping("/{id}")"""

content = content.replace("    @GetMapping(\"/{id}\")", new_method)

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'w') as f:
    f.write(content)
