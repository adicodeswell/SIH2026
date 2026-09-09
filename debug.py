file_path = 'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/VerificationServiceTest.java'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace("assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());", "if (res.getOverallStatus() != VerificationStatus.VERIFIED) { System.out.println(res.getReasons().stream().map(r -> r.getCode() + \":\" + r.getFieldName() + \":\" + r.getSource()).toList()); } assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());")
with open(file_path, 'w') as f:
    f.write(content)
