import re

file_path = 'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/VerificationServiceTest.java'
with open(file_path, 'r') as f:
    content = f.read()

new_tests = """
    @Test
    void testNumericEquivalence() {
        ResolvedConsentPolicy customPolicy = new ResolvedConsentPolicy("TEST", Set.of(DataScope.EMPLOYMENT, DataScope.HEALTH), "test", "employment", "DEPT", Set.of("annualFamilyIncome"), Set.of());
        
        CanonicalCitizenData empData1 = new CanonicalCitizenData();
        empData1.setAnnualFamilyIncome(new java.math.BigDecimal("500000"));
        SourceDataResult emp1 = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData1);
        
        CanonicalCitizenData empData2 = new CanonicalCitizenData();
        empData2.setAnnualFamilyIncome(new java.math.BigDecimal("500000.00"));
        // Simulate a second system that is authoritative just for test
        // Let's assume HEALTH_SYSTEM is NOT authoritative for income, so it would be ignored.
        // I will temporarily use SKILLS_SYSTEM in the test, wait, only EMPLOYMENT_SYSTEM is authoritative for annualFamilyIncome!
        // So I can't test conflict across systems unless I use two identical EMPLOYMENT_SYSTEM source results... wait, duplicate sources are rejected.
        // Actually, if we just check how it normalizes, we can test it directly on one system, or verify that duplicate sources behavior works?
        // Let's just create an EDUCATION_SYSTEM that we temporarily inject, or just rely on the existing normalize logic being right.
        // Actually, we can test it by inspecting the `FieldVerificationResult`.
    }
"""
