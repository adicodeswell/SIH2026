import os

file = 'backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/dto/WorkflowStatusCallback.java'
with open(file, 'r') as f:
    content = f.read()

fields = """
    private String officerDecision;
    private String officerDecisionReason;

    public String getOfficerDecision() { return officerDecision; }
    public void setOfficerDecision(String officerDecision) { this.officerDecision = officerDecision; }

    public String getOfficerDecisionReason() { return officerDecisionReason; }
    public void setOfficerDecisionReason(String officerDecisionReason) { this.officerDecisionReason = officerDecisionReason; }
"""

content = content.replace("private Object verificationData;", "private Object verificationData;" + fields)
with open(file, 'w') as f:
    f.write(content)

file2 = 'backend/application-service/src/main/java/com/mahasetu/application/dto/WorkflowStatusCallbackRequest.java'
with open(file2, 'r') as f:
    content2 = f.read()

content2 = content2.replace("private Object verificationData;", "private Object verificationData;" + fields)
with open(file2, 'w') as f:
    f.write(content2)

