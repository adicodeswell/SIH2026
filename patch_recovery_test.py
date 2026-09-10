import re

with open('./backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationWorkflowRecoveryTest.java', 'r') as f:
    content = f.read()

# We need to change the test so that submitApplication fails, rather than createApplication.
# Let's mock ConsentClient for submitApplication
content = content.replace("private WorkflowClient workflowClient;", "private WorkflowClient workflowClient;\n    @org.mockito.Mock\n    private com.mahasetu.application.integration.ConsentClient consentClient;")

old_test_part = """        // Act
        ApplicationResponse response = applicationService.createApplication(request);"""

new_test_part = """        // Act
        org.mockito.Mockito.when(consentClient.hasConsentForApplication(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        ApplicationResponse createResponse = applicationService.createApplication(request);
        ApplicationResponse response = applicationService.submitApplication(createResponse.getApplicationNumber(), "MH1001", consentClient);"""
        
content = content.replace(old_test_part, new_test_part)

with open('./backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationWorkflowRecoveryTest.java', 'w') as f:
    f.write(content)
