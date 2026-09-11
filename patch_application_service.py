import re

file = 'backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java'
with open(file, 'r') as f:
    content = f.read()

# Replace the OFFICER_REVIEW parsing logic
old_logic = """                    } else if ("OFFICER_REVIEW".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        String meta = (String) log.get("metadata");
                        if (meta != null && meta.contains("decision=APPROVE")) {
                            dto.setTitle("Application Approved");
                            dto.setDescription("Application approved by the reviewing officer.");
                            dto.setStatus("APPROVED");
                        } else if (meta != null && meta.contains("decision=REJECT")) {
                            dto.setTitle("Application Rejected");
                            dto.setDescription("Application rejected by the reviewing officer.");
                            dto.setStatus("REJECTED");
                        } else {
                            dto.setTitle("Officer Review");
                            dto.setDescription("Officer review completed.");
                        }
                        dto.setActorType("OFFICER");"""

new_logic = """                    } else if ("OFFICER_REVIEW".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        String purpose = (String) log.get("purpose");
                        String meta = (String) log.get("metadata");
                        
                        String reason = null;
                        if (meta != null && !meta.trim().isEmpty()) {
                            try {
                                java.util.Map<String, Object> metaMap = objectMapper.readValue(meta, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                                if (metaMap.containsKey("reason")) {
                                    reason = (String) metaMap.get("reason");
                                }
                            } catch (Exception e) {
                                // Ignore parse error, fallback to default description
                            }
                        }

                        if ("APPROVE".equals(purpose)) {
                            dto.setTitle("Application Approved");
                            dto.setStatus("APPROVED");
                            if (reason != null && !reason.trim().isEmpty()) {
                                dto.setDescription(reason);
                            } else {
                                dto.setDescription("Application approved by the reviewing officer.");
                            }
                        } else if ("REJECT".equals(purpose)) {
                            dto.setTitle("Application Rejected");
                            dto.setStatus("REJECTED");
                            if (reason != null && !reason.trim().isEmpty()) {
                                dto.setDescription(reason);
                            } else {
                                dto.setDescription("Application rejected by the reviewing officer.");
                            }
                        } else {
                            dto.setTitle("Officer Review");
                            dto.setDescription("Officer review completed.");
                        }
                        dto.setActorType("OFFICER");"""

content = content.replace(old_logic, new_logic)

with open(file, 'w') as f:
    f.write(content)

