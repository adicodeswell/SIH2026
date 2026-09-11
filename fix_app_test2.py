import re

file = 'backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationServiceTest.java'
with open(file, 'r') as f:
    content = f.read()

content = content.replace('existingApp = new Application();\n        existingApp.setApplicationNumber("MH-2026-000001");\n        existingApp.setCitizen(citizen);\n        existingApp.setService(serviceEntity);\n        existingApp.setStatus(ApplicationStatus.SUBMITTED);', 'existingApp = new Application();\n        existingApp.setApplicationNumber("MH-2026-000001");\n        existingApp.setCitizen(citizen);\n        existingApp.setService(serviceEntity);\n        existingApp.setStatus(ApplicationStatus.DRAFT);')

with open(file, 'w') as f:
    f.write(content)

