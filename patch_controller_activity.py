with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'r') as f:
    content = f.read()

new_method = """    @GetMapping("/{id}/activity")
    public List<com.mahasetu.application.dto.CitizenApplicationActivityResponse> getApplicationActivity(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        if (authentication != null) {
            boolean isOfficerOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"));
                
            boolean isService = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
            
            if (!isOfficerOrAdmin && !isService) {
                String username = authentication.getName();
                if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
                    String claim = jwtAuth.getToken().getClaimAsString("preferred_username");
                    if (claim != null) username = claim;
                }
                
                ApplicationResponse response = applicationService.getApplication(id);
                if (username != null && !username.equalsIgnoreCase(response.getCitizenId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Cannot access application belonging to another citizen");
                }
            }
        }
        return applicationService.getApplicationActivity(id);
    }

    @GetMapping("/{id}/timeline")"""

content = content.replace("    @GetMapping(\"/{id}/timeline\")", new_method)

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'w') as f:
    f.write(content)
