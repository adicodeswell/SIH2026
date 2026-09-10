with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/AuditService.java', 'r') as f:
    content = f.read()

import re

old_record1 = """    public AuditLog recordConsentGranted(String citizenId, UUID consentId, String departmentId, String dataScope, String purpose) {
        log.info("Recording consent granted audit: citizenId={}, consentId={}, departmentId={}",
                citizenId, consentId, departmentId);
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("departmentId", departmentId);
        metadataMap.put("dataScope", dataScope);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLog auditLog = new AuditLog(
                null,
                citizenId,
                "CONSENT_GRANTED",
                "CONSENT",
                consentId.toString(),
                purpose,
                metadataJson
        );
        return auditLogRepository.save(auditLog);
    }"""

new_record1 = """    public AuditLog recordConsentGranted(String citizenId, UUID consentId, String departmentId, String dataScope, String purpose, String applicationId) {
        log.info("Recording consent granted audit: citizenId={}, consentId={}, departmentId={}",
                citizenId, consentId, departmentId);
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("departmentId", departmentId);
        metadataMap.put("dataScope", dataScope);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLog auditLog = new AuditLog(
                applicationId,
                citizenId,
                "CONSENT_GRANTED",
                "CONSENT",
                consentId.toString(),
                purpose,
                metadataJson
        );
        return auditLogRepository.save(auditLog);
    }"""

old_record2 = """    public AuditLog recordConsentRevoked(String citizenId, UUID consentId) {
        log.info("Recording consent revoked audit: citizenId={}, consentId={}", citizenId, consentId);
        AuditLog auditLog = new AuditLog(
                null,
                citizenId,
                "CONSENT_REVOKED",
                "CONSENT",
                consentId.toString(),
                "REVOCATION",
                "{}"
        );
        return auditLogRepository.save(auditLog);
    }"""

new_record2 = """    public AuditLog recordConsentRevoked(String citizenId, UUID consentId, String applicationId) {
        log.info("Recording consent revoked audit: citizenId={}, consentId={}", citizenId, consentId);
        AuditLog auditLog = new AuditLog(
                applicationId,
                citizenId,
                "CONSENT_REVOKED",
                "CONSENT",
                consentId.toString(),
                "REVOCATION",
                "{}"
        );
        return auditLogRepository.save(auditLog);
    }"""

content = content.replace(old_record1, new_record1)
content = content.replace(old_record2, new_record2)

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/AuditService.java', 'w') as f:
    f.write(content)

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/ConsentService.java', 'r') as f:
    consent_content = f.read()

consent_content = consent_content.replace("auditService.recordConsentGranted(citizenId, saved.getId(), saved.getRequestingDepartmentId(),\n                saved.getDataScope(), saved.getPurpose());", "auditService.recordConsentGranted(citizenId, saved.getId(), saved.getRequestingDepartmentId(),\n                saved.getDataScope(), saved.getPurpose(), saved.getApplicationId());")

consent_content = consent_content.replace("auditService.recordConsentRevoked(citizenId, consentId);", "auditService.recordConsentRevoked(citizenId, consentId, consent.getApplicationId());")

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/ConsentService.java', 'w') as f:
    f.write(consent_content)

