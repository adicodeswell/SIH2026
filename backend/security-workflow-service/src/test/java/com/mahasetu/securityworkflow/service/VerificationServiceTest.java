package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import com.mahasetu.securityworkflow.dto.DataScope;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
import com.mahasetu.securityworkflow.dto.verification.FieldVerificationResult;
import com.mahasetu.securityworkflow.dto.verification.FieldVerificationStatus;
import com.mahasetu.securityworkflow.dto.verification.VerificationResult;
import com.mahasetu.securityworkflow.dto.verification.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VerificationServiceTest {

    private VerificationService verificationService;
    private ResolvedConsentPolicy policy;

    @BeforeEach
    void setUp() {
        verificationService = new VerificationService();
        policy = new ResolvedConsentPolicy(
                "TEST_SERVICE", 
                Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT), 
                "verification", 
                "education,employment", 
                "DEPT-TEST",
                Set.of("fullName", "highestDegree"),
                Set.of("annualFamilyIncome")
        );
    }

    private SourceDataResult createSourceResult(String source, String status, String error, CanonicalCitizenData data) {
        SourceDataResult res = new SourceDataResult();
        res.setSource(source);
        res.setStatus(status);
        res.setError(error);
        res.setData(data);
        return res;
    }

    @Test
    void testAllRequiredFieldsMatch_Verified() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);

        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setFullName("Aditya Jha");
        empData.setAnnualFamilyIncome(500000.0);
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
    }

    @Test
    void testRequiredNameMismatch_Conflict() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);

        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setFullName("Aditya Jah"); // Mismatch
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.NOT_VERIFIED, res.getOverallStatus());
        
        FieldVerificationResult fRes = res.getFieldResults().stream().filter(f -> f.getFieldName().equals("fullName")).findFirst().get();
        assertEquals(FieldVerificationStatus.CONFLICT, fRes.getStatus());
    }

    @Test
    void testMissingFieldVsProvided_NotConflict() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);

        CanonicalCitizenData empData = new CanonicalCitizenData();
        // empData does not provide fullName
        empData.setAnnualFamilyIncome(500000.0);
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
    }

    @Test
    void testMissingRequiredField_Partial() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        // highestDegree is required but missing from Education
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);
        
        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setFullName("Aditya Jha");
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.PARTIALLY_VERIFIED, res.getOverallStatus());
    }

    @Test
    void testWhitespaceCaseNormalization() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName(" Aditya   Jha ");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);

        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setFullName("aditya jha");
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
    }

    @Test
    void testRequiredSourceFailed() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);

        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "FAILED", "Service Unavailable", null);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.PARTIALLY_VERIFIED, res.getOverallStatus());
    }

    @Test
    void testUnknownSource_Rejected() {
        SourceDataResult edu = createSourceResult("UNKNOWN_SYSTEM", "SUCCESS", null, new CanonicalCitizenData());
        VerificationResult res = verificationService.verify(List.of(edu), policy);
        assertEquals(VerificationStatus.UNABLE_TO_VERIFY, res.getOverallStatus());
        assertEquals("FAILED", res.getSourceResults().get(0).getStatus());
    }

    @Test
    void testMasking() {
        ResolvedConsentPolicy customPolicy = new ResolvedConsentPolicy("TEST", Set.of(DataScope.EMPLOYMENT), "test", "employment", "DEPT", Set.of("aadhaarNumber"), Set.of());
        
        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setAadhaarNumber("123456789012");
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);
        
        VerificationResult res = verificationService.verify(List.of(emp), customPolicy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
        String maskedAadhaar = res.getFieldResults().get(0).getProvidedValues().get("EMPLOYMENT_SYSTEM");
        assertEquals("********9012", maskedAadhaar);
    }
}
