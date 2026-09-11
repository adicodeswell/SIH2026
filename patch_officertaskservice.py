import re

with open('backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/OfficerTaskService.java', 'r') as f:
    content = f.read()

# Add import
if 'com.fasterxml.jackson.databind.ObjectMapper' not in content:
    content = content.replace('import org.springframework.stereotype.Service;', 'import com.fasterxml.jackson.databind.ObjectMapper;\nimport org.springframework.stereotype.Service;')

# Add final field
if 'private final ObjectMapper objectMapper;' not in content:
    content = content.replace('private final ConsentPolicyService consentPolicyService;', 'private final ConsentPolicyService consentPolicyService;\n    private final ObjectMapper objectMapper;')

# Replace constructor
old_constructor = 'public OfficerTaskService(TaskService taskService, HistoryService historyService, AuditService auditService, ConsentPolicyService consentPolicyService) {\n        this.taskService = taskService;\n        this.historyService = historyService;\n        this.auditService = auditService;\n        this.consentPolicyService = consentPolicyService;\n    }'
new_constructor = 'public OfficerTaskService(TaskService taskService, HistoryService historyService, AuditService auditService, ConsentPolicyService consentPolicyService, ObjectMapper objectMapper) {\n        this.taskService = taskService;\n        this.historyService = historyService;\n        this.auditService = auditService;\n        this.consentPolicyService = consentPolicyService;\n        this.objectMapper = objectMapper;\n    }'
content = content.replace(old_constructor, new_constructor)

# Replace mapToResponse
old_map_to_response = """    private OfficerReviewTaskResponse mapToResponse(Task task) {
        Map<String, Object> variables = taskService.getVariables(task.getId());
        String applicationId = (String) variables.get("applicationId");
        String citizenId = (String) variables.get("citizenId");
        String serviceCode = (String) variables.get("serviceCode");

        return new OfficerReviewTaskResponse(
                task.getId(),
                task.getName(),
                applicationId,
                task.getProcessInstanceId(),
                citizenId,
                serviceCode,
                task.getCreateTime(),
                "OFFICER",
                task.getAssignee(),
                "PENDING_REVIEW"
        );
    }"""
new_map_to_response = """    private OfficerReviewTaskResponse mapToResponse(Task task) {
        Map<String, Object> variables = taskService.getVariables(task.getId());
        String applicationId = (String) variables.get("applicationId");
        String citizenId = (String) variables.get("citizenId");
        String serviceCode = (String) variables.get("serviceCode");

        OfficerReviewTaskResponse response = new OfficerReviewTaskResponse(
                task.getId(),
                task.getName(),
                applicationId,
                task.getProcessInstanceId(),
                citizenId,
                serviceCode,
                task.getCreateTime(),
                "OFFICER",
                task.getAssignee(),
                "PENDING_REVIEW"
        );
        
        Object verificationResultVar = variables.get("verificationResult");
        if (verificationResultVar instanceof String) {
            try {
                Object verificationData = objectMapper.readValue((String) verificationResultVar, Object.class);
                response.setVerificationData(verificationData);
            } catch (Exception e) {
                log.warn("Failed to parse verificationResult JSON for task {}", task.getId());
            }
        } else if (verificationResultVar != null) {
            response.setVerificationData(verificationResultVar);
        }

        return response;
    }"""
content = content.replace(old_map_to_response, new_map_to_response)

with open('backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/OfficerTaskService.java', 'w') as f:
    f.write(content)

