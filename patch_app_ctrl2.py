import re

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'r') as f:
    content = f.read()

old_get = """    @GetMapping("/{id}")
    public ApplicationResponse getApplicationDetails(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        ApplicationResponse response = applicationService.getApplication(id);
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            response.setVerificationData(null);
        }
        return response;
    }"""

new_get = """    @GetMapping("/{id}")
    public ApplicationResponse getApplicationDetails(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        ApplicationResponse response = applicationService.getApplication(id);
        
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
                
                if (username != null && !username.equalsIgnoreCase(response.getCitizenId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Cannot access application belonging to another citizen");
                }
            }
            
            if (!isOfficerOrAdmin && !isService) {
                response.setVerificationData(null);
            }
        }
        
        return response;
    }"""

content = content.replace(old_get, new_get)

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'w') as f:
    f.write(content)
