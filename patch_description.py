import re

file = 'backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java'
with open(file, 'r') as f:
    content = f.read()

old_logic = """    private String workflowEventDescription(WorkflowStatusCallbackRequest request) {
        if (request.getFailureReason() != null && !request.getFailureReason().isBlank()) {
            return request.getFailureReason();
        }
        return "Workflow status callback received: " + request.getStatus();
    }"""

new_logic = """    private String workflowEventDescription(WorkflowStatusCallbackRequest request) {
        if (request.getOfficerDecisionReason() != null && !request.getOfficerDecisionReason().isBlank()) {
            return request.getOfficerDecisionReason();
        }
        if (request.getFailureReason() != null && !request.getFailureReason().isBlank()) {
            return request.getFailureReason();
        }
        return "Workflow status callback received: " + request.getStatus();
    }"""

content = content.replace(old_logic, new_logic)

with open(file, 'w') as f:
    f.write(content)

