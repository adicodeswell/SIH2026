with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'r') as f:
    content = f.read()

old_code = """        try {
            workflowClient.startWorkflow(savedApplication.getApplicationNumber(), workflowKey);
            recordEvent(savedApplication, "WORKFLOW_STARTED", ApplicationStatus.SUBMITTED, ApplicationStatus.SUBMITTED,
                    "Workflow started: " + workflowKey, "application-service");
        } catch (Exception e) {
            log.error("Failed to start workflow", e);
            savedApplication.setStatus(ApplicationStatus.FAILED);
            savedApplication = applicationRepository.save(savedApplication);
            recordEvent(savedApplication, "WORKFLOW_START_FAILED", ApplicationStatus.SUBMITTED, ApplicationStatus.FAILED,
                    "Workflow start failed", "application-service");
        }

        return mapToResponse(savedApplication);"""
        
new_code = """        try {
            workflowClient.startWorkflow(savedApplication.getApplicationNumber(), workflowKey);
            recordEvent(savedApplication, "WORKFLOW_STARTED", ApplicationStatus.SUBMITTED, ApplicationStatus.SUBMITTED,
                    "Workflow started: " + workflowKey, "application-service");
        } catch (Exception e) {
            log.error("Failed to start workflow", e);
            savedApplication.setStatus(ApplicationStatus.FAILED);
            savedApplication = applicationRepository.save(savedApplication);
            recordEvent(savedApplication, "WORKFLOW_START_FAILED", ApplicationStatus.SUBMITTED, ApplicationStatus.FAILED,
                    "Workflow start failed", "application-service");
            throw new RuntimeException("Workflow failed to start. Application submission aborted.");
        }

        return mapToResponse(savedApplication);"""

content = content.replace(old_code, new_code)

with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'w') as f:
    f.write(content)
