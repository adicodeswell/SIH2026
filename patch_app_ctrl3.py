import re

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'r') as f:
    content = f.read()

# Fix parameter and constructor
content = content.replace("private final ApplicationService applicationService;", "private final ApplicationService applicationService;\n    private final com.mahasetu.application.integration.ConsentClient consentClient;")

old_constructor = """    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }"""
    
new_constructor = """    public ApplicationController(ApplicationService applicationService, com.mahasetu.application.integration.ConsentClient consentClient) {
        this.applicationService = applicationService;
        this.consentClient = consentClient;
    }"""
    
content = content.replace(old_constructor, new_constructor)

old_submit = """    public ApplicationResponse submitApplication(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication, org.springframework.beans.factory.annotation.Autowired com.mahasetu.application.integration.ConsentClient consentClient) {"""
new_submit = """    public ApplicationResponse submitApplication(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {"""

content = content.replace(old_submit, new_submit)

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'w') as f:
    f.write(content)
