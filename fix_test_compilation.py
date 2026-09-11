import re
import os

files = [
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/WorkflowResilienceAndHardeningTest.java',
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/AuditServiceTest.java'
]

for file in files:
    with open(file, 'r') as f:
        content = f.read()

    # Fix recordConsentGranted (add null as 6th argument)
    content = re.sub(r'recordConsentGranted\((.*?),\s*(.*?),\s*(.*?),\s*(.*?),\s*(.*?)\)', r'recordConsentGranted(\1, \2, \3, \4, \5, null)', content)

    # Fix recordConsentRevoked (add null as 3rd argument)
    content = re.sub(r'recordConsentRevoked\((.*?),\s*(.*?)\)', r'recordConsentRevoked(\1, \2, null)', content)

    with open(file, 'w') as f:
        f.write(content)

