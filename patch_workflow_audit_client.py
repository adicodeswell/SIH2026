with open('./backend/application-service/src/main/java/com/mahasetu/application/integration/WorkflowAuditClient.java', 'r') as f:
    content = f.read()
    
content = content.replace("headers.setBearerAuth(tokenProvider.getServiceToken());", "headers.set(\"Authorization\", tokenProvider.getAuthorizationHeader());")

with open('./backend/application-service/src/main/java/com/mahasetu/application/integration/WorkflowAuditClient.java', 'w') as f:
    f.write(content)
