import re

file = 'backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java'
with open(file, 'r') as f:
    content = f.read()

old_logic = """            } else {
                dto.setCategory("GENERAL");
                dto.setTitle(event.getEventType());
                dto.setDescription(event.getDescription());
                dto.setActorType("SYSTEM");
            }"""

new_logic = """            } else if ("WORKFLOW_APPROVED".equals(event.getEventType()) || 
                       "WORKFLOW_REJECTED".equals(event.getEventType()) || 
                       "WORKFLOW_PENDING_REVIEW".equals(event.getEventType())) {
                continue; // Skip these as they are covered by AuditLog (OFFICER_REVIEW, OFFICER_CLAIM, etc)
            } else {
                dto.setCategory("GENERAL");
                dto.setTitle(event.getEventType());
                dto.setDescription(event.getDescription());
                dto.setActorType("SYSTEM");
            }"""

content = content.replace(old_logic, new_logic)

with open(file, 'w') as f:
    f.write(content)

