with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'r') as f:
    content = f.read()

import re

new_imports = """import com.mahasetu.application.dto.CitizenApplicationActivityResponse;
import com.mahasetu.application.integration.WorkflowAuditClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;
"""

if "import com.mahasetu.application.dto.CitizenApplicationActivityResponse;" not in content:
    content = content.replace("import com.mahasetu.application.integration.WorkflowClient;", "import com.mahasetu.application.integration.WorkflowClient;\n" + new_imports)

# We need to add WorkflowAuditClient to constructor
if "private final WorkflowAuditClient workflowAuditClient;" not in content:
    content = content.replace("private final WorkflowClient workflowClient;", "private final WorkflowClient workflowClient;\n    private final WorkflowAuditClient workflowAuditClient;")
    content = content.replace("WorkflowClient workflowClient) {", "WorkflowClient workflowClient,\n                              WorkflowAuditClient workflowAuditClient) {")
    content = content.replace("this.workflowClient = workflowClient;", "this.workflowClient = workflowClient;\n        this.workflowAuditClient = workflowAuditClient;")

# Add getApplicationActivity method
new_method = """    public List<CitizenApplicationActivityResponse> getApplicationActivity(String applicationNumber) {
        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new com.mahasetu.application.exception.ResourceNotFoundException("Application not found"));

        List<CitizenApplicationActivityResponse> activities = new ArrayList<>();

        // Add application events
        List<com.mahasetu.application.entity.ApplicationEvent> events = eventRepository.findByApplication_ApplicationNumberOrderByOccurredAtAsc(applicationNumber);
        for (com.mahasetu.application.entity.ApplicationEvent event : events) {
            CitizenApplicationActivityResponse dto = new CitizenApplicationActivityResponse();
            dto.setId(event.getId().toString());
            dto.setType(event.getEventType());
            dto.setStatus(event.getNewStatus() != null ? event.getNewStatus().name() : null);
            dto.setOccurredAt(event.getOccurredAt());
            
            // Normalize
            if ("APPLICATION_CREATED".equals(event.getEventType())) {
                dto.setCategory("APPLICATION");
                dto.setTitle("Application Created");
                dto.setDescription("Application draft was created.");
                dto.setActorType("CITIZEN");
            } else if ("APPLICATION_SUBMITTED".equals(event.getEventType())) {
                dto.setCategory("APPLICATION");
                dto.setTitle("Application Submitted");
                dto.setDescription("Application was successfully submitted.");
                dto.setActorType("CITIZEN");
            } else if ("WORKFLOW_STARTED".equals(event.getEventType())) {
                dto.setCategory("WORKFLOW");
                dto.setTitle("Verification Started");
                dto.setDescription("Automated verification process has started.");
                dto.setActorType("SYSTEM");
            } else if ("WORKFLOW_START_FAILED".equals(event.getEventType())) {
                dto.setCategory("WORKFLOW");
                dto.setTitle("Verification Failed");
                dto.setDescription("Application processing encountered an issue.");
                dto.setActorType("SYSTEM");
            } else {
                dto.setCategory("GENERAL");
                dto.setTitle(event.getEventType());
                dto.setDescription(event.getDescription());
                dto.setActorType("SYSTEM");
            }
            activities.add(dto);
        }

        // Add security workflow audit logs
        try {
            List<Map<String, Object>> auditLogs = workflowAuditClient.getApplicationAuditLogs(applicationNumber);
            if (auditLogs != null) {
                for (Map<String, Object> log : auditLogs) {
                    String action = (String) log.get("action");
                    String occurredAtStr = (String) log.get("occurredAt");
                    LocalDateTime occurredAt = null;
                    if (occurredAtStr != null) {
                        try {
                            occurredAt = LocalDateTime.parse(occurredAtStr.replace("Z", ""));
                        } catch(Exception ignored) {}
                    }
                    
                    if (action == null || occurredAt == null) continue;

                    CitizenApplicationActivityResponse dto = new CitizenApplicationActivityResponse();
                    dto.setId((String) log.get("id"));
                    dto.setType(action);
                    dto.setOccurredAt(occurredAt);
                    
                    if ("CONSENT_GRANTED".equals(action)) {
                        dto.setCategory("CONSENT");
                        dto.setTitle("Consent Granted");
                        dto.setDescription("Digital consent granted for verification.");
                        dto.setActorType("CITIZEN");
                    } else if ("CONSENT_REVOKED".equals(action)) {
                        dto.setCategory("CONSENT");
                        dto.setTitle("Consent Revoked");
                        dto.setDescription("Digital consent revoked.");
                        dto.setActorType("CITIZEN");
                    } else if ("OFFICER_REVIEW".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        String meta = (String) log.get("metadata");
                        if (meta != null && meta.contains("decision=APPROVE")) {
                            dto.setTitle("Application Approved");
                            dto.setDescription("Application approved by the reviewing officer.");
                            dto.setStatus("APPROVED");
                        } else if (meta != null && meta.contains("decision=REJECT")) {
                            dto.setTitle("Application Rejected");
                            dto.setDescription("Application rejected by the reviewing officer.");
                            dto.setStatus("REJECTED");
                        } else {
                            dto.setTitle("Officer Review");
                            dto.setDescription("Officer review completed.");
                        }
                        dto.setActorType("OFFICER");
                    } else if ("OFFICER_CLAIM".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        dto.setTitle("Officer Assigned");
                        dto.setDescription("Application review assigned to a government officer.");
                        dto.setActorType("OFFICER");
                    } else if ("OFFICER_UNCLAIM".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        dto.setTitle("Officer Unassigned");
                        dto.setDescription("Application review was returned to the review queue.");
                        dto.setActorType("OFFICER");
                    } else {
                        // Skip raw internal events like WORKFLOW_STARTED from workflow DB to avoid duplicates
                        if ("WORKFLOW_STARTED".equals(action)) continue;
                        
                        dto.setCategory("WORKFLOW");
                        dto.setTitle(action);
                        dto.setDescription("System activity.");
                        dto.setActorType("SYSTEM");
                    }
                    activities.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch workflow audit logs for application {}", applicationNumber, e);
            // Optionally add a fake event indicating partial unavailability
            CitizenApplicationActivityResponse err = new CitizenApplicationActivityResponse();
            err.setId("error");
            err.setType("ACTIVITY_PARTIALLY_UNAVAILABLE");
            err.setCategory("SYSTEM");
            err.setTitle("Activity Partially Unavailable");
            err.setDescription("Some workflow activities could not be loaded at this time.");
            err.setOccurredAt(LocalDateTime.now());
            err.setActorType("SYSTEM");
            activities.add(err);
        }

        activities.sort(Comparator.comparing(CitizenApplicationActivityResponse::getOccurredAt));
        return activities;
    }
"""

content = content.replace("public ApplicationResponse getApplication(", new_method + "\n    public ApplicationResponse getApplication(")

with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'w') as f:
    f.write(content)
