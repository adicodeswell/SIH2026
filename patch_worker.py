import os

file = 'backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/worker/StatusCallbackWorker.java'
with open(file, 'r') as f:
    content = f.read()

content = content.replace('String officerId = (String) execution.getVariable("officerId");', 'String officerId = (String) execution.getVariable("officerId");\n        String officerDecision = (String) execution.getVariable("officerDecision");\n        String officerDecisionReason = (String) execution.getVariable("officerDecisionReason");')

setup = """WorkflowStatusCallback callback = new WorkflowStatusCallback(
                applicationId,
                processInstanceId,
                workflowStatus,
                failureReason,
                officerId
        );
        callback.setOfficerDecision(officerDecision);
        callback.setOfficerDecisionReason(officerDecisionReason);
"""

content = content.replace("""WorkflowStatusCallback callback = new WorkflowStatusCallback(
                applicationId,
                processInstanceId,
                workflowStatus,
                failureReason,
                officerId
        );""", setup)

with open(file, 'w') as f:
    f.write(content)

