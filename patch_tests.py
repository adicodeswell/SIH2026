import re

with open('./backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationServiceTest.java', 'r') as f:
    content = f.read()

# Replace testCreateApplication_Success expectations
old_test = """        verify(workflowClient, times(1)).startWorkflow("MH-2026-000001", "application-orchestration");
        assertEquals(ApplicationStatus.SUBMITTED, response.getStatus());"""
new_test = """        verify(workflowClient, never()).startWorkflow(anyString(), anyString());
        assertEquals(ApplicationStatus.DRAFT, response.getStatus());"""
content = content.replace(old_test, new_test)

with open('./backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationServiceTest.java', 'w') as f:
    f.write(content)
