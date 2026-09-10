with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/repository/AuditLogRepository.java', 'r') as f:
    content = f.read()
    
content = content.replace("List<AuditLog> findByApplicationId(String applicationId);", "List<AuditLog> findByApplicationId(String applicationId);\n    List<AuditLog> findByApplicationIdOrderByOccurredAtAsc(String applicationId);")

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/repository/AuditLogRepository.java', 'w') as f:
    f.write(content)
