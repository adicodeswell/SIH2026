with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/worker/StatusCallbackWorker.java', 'r') as f:
    content = f.read()

old_call = "workflowStatusClient.sendStatusCallback(callback);"
new_call = """try {
            workflowStatusClient.sendStatusCallback(callback);
            log.info("[WORKFLOW_EVENT] Callback successfully sent for application {}", applicationId);
        } catch (Exception e) {
            log.warn("[WORKFLOW_EVENT] Callback failed for application {}, but workflow will continue: {}", applicationId, e.getMessage());
            // Do NOT throw exception. Failure here must not stop the main workflow progression (e.g. reaching Officer Review).
        }"""
content = content.replace(old_call, new_call)

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/worker/StatusCallbackWorker.java', 'w') as f:
    f.write(content)
