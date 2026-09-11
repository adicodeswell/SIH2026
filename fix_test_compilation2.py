import os

file = 'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/WorkflowResilienceAndHardeningTest.java'
with open(file, 'r') as f:
    content = f.read()

content = content.replace('eq("job_verification", null)', 'eq("job_verification"), org.mockito.ArgumentMatchers.anyString()')

# also check if I messed up anything else
content = content.replace('recordConsentRevoked(eq(citizenId), eq(consentId), null)', 'recordConsentRevoked(eq(citizenId), eq(consentId), org.mockito.ArgumentMatchers.anyString())')

with open(file, 'w') as f:
    f.write(content)

file2 = 'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/AuditServiceTest.java'
with open(file2, 'r') as f:
    content = f.read()

content = content.replace('eq("Housing subsidy verification", null)', 'eq("Housing subsidy verification"), org.mockito.ArgumentMatchers.anyString()')

with open(file2, 'w') as f:
    f.write(content)

