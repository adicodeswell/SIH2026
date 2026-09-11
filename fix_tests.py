import re

file = 'backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationServiceTest.java'
with open(file, 'r') as f:
    content = f.read()

content = content.replace('req.setOfficerDecision("APPROVE");\n        req.setOfficerDecisionReason("Approval note");', 'req.setOfficerDecision("APPROVE");\n        req.setOfficerDecisionReason("Approval note");\n        when(applicationRepository.save(any(Application.class))).thenReturn(existingApp);')
content = content.replace('req.setOfficerDecision("REJECT");\n        req.setOfficerDecisionReason("Rejection reason");', 'req.setOfficerDecision("REJECT");\n        req.setOfficerDecisionReason("Rejection reason");\n        when(applicationRepository.save(any(Application.class))).thenReturn(existingApp);')

with open(file, 'w') as f:
    f.write(content)

