import re

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/ConsentService.java', 'r') as f:
    content = f.read()

# Add hasGrantedConsentForApplication method
method = """
    public boolean hasGrantedConsentForApplication(String applicationId) {
        return consentRepository.findFirstByApplicationIdAndStatusOrderByGrantedAtDesc(applicationId, "GRANTED").isPresent();
    }
"""

if "hasGrantedConsentForApplication" not in content:
    content = content.replace("public List<Consent> getConsents", method + "\n    public List<Consent> getConsents")

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/ConsentService.java', 'w') as f:
    f.write(content)
