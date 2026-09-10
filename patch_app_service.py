import re

with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'r') as f:
    content = f.read()

# 1. Modify createApplication
old_create = """        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());

        Application savedApplication = applicationRepository.save(application);

        recordEvent(savedApplication, "APPLICATION_SUBMITTED", null, ApplicationStatus.SUBMITTED, "Application created and submitted", request.getCitizenId());

        String workflowKey = service.getWorkflowKey() != null && !service.getWorkflowKey().isBlank()
                ? service.getWorkflowKey()
                : "application-orchestration";

        try {
            workflowClient.startWorkflow(savedApplication.getApplicationNumber(), workflowKey);
            recordEvent(savedApplication, "WORKFLOW_STARTED", ApplicationStatus.SUBMITTED, ApplicationStatus.SUBMITTED,
                    "Workflow started: " + workflowKey, "application-service");
        } catch (Exception e) {
            e.printStackTrace();
            ApplicationStatus oldStatus = savedApplication.getStatus();
            savedApplication.setStatus(ApplicationStatus.FAILED);
            savedApplication = applicationRepository.save(savedApplication);
            recordEvent(savedApplication, "WORKFLOW_START_FAILED", oldStatus, ApplicationStatus.FAILED,
                    "Workflow start failed", "application-service");
        }

        return mapToResponse(savedApplication);"""

new_create = """        application.setStatus(ApplicationStatus.DRAFT);
        application.setSubmittedAt(LocalDateTime.now()); // Or null? The original sets it here. Let's keep it or set it on submit.

        Application savedApplication = applicationRepository.save(application);

        recordEvent(savedApplication, "APPLICATION_CREATED", null, ApplicationStatus.DRAFT, "Application created", request.getCitizenId());

        return mapToResponse(savedApplication);"""

content = content.replace(old_create, new_create)

# 2. Add submitApplication
submit_method = """
    @Transactional
    public ApplicationResponse submitApplication(String applicationNumber, String citizenId, com.mahasetu.application.integration.ConsentClient consentClient) {
        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationNumber));
                
        if (!application.getCitizen().getCitizenId().equals(citizenId)) {
             throw new org.springframework.security.access.AccessDeniedException("Cannot submit an application you do not own");
        }

        if (application.getStatus() != ApplicationStatus.DRAFT) {
             throw new ValidationException("Only DRAFT applications can be submitted");
        }

        boolean hasConsent = consentClient.hasConsentForApplication(applicationNumber);
        if (!hasConsent) {
             throw new ValidationException("Cannot submit application: Required consent is not granted");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());
        Application savedApplication = applicationRepository.save(application);

        recordEvent(savedApplication, "APPLICATION_SUBMITTED", oldStatus, ApplicationStatus.SUBMITTED, "Application submitted", citizenId);

        String workflowKey = application.getService().getWorkflowKey() != null && !application.getService().getWorkflowKey().isBlank()
                ? application.getService().getWorkflowKey()
                : "application-orchestration";

        try {
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

        return mapToResponse(savedApplication);
    }
"""

if "submitApplication" not in content:
    content = content.replace("public ApplicationResponse applyWorkflowStatusCallback", submit_method + "\n    public ApplicationResponse applyWorkflowStatusCallback")


with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'w') as f:
    f.write(content)
