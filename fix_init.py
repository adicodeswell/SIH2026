import re
file_path = 'backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/worker/InitializeApplicationWorker.java'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('throw new BpmnError("APPLICATION_FETCH_FAILED", "Error fetching application: " + e.getMessage());', 'throw new BpmnError("APPLICATION_FETCH_FAILED", "APPLICATION_FETCH_UNAVAILABLE");')

with open(file_path, 'w') as f:
    f.write(content)
