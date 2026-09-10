package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import com.mahasetu.securityworkflow.dto.DataScope;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
import com.mahasetu.securityworkflow.dto.verification.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
                Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT, DataScope.HEALTH), 
                "verification", 
                "education,employment,health", 
                "DEPT-TEST",
                Set.of("fullName", "highestDegree"),
                Set.of("annualFamilyIncome", "bloodGroup")
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
        empData.setAnnualFamilyIncome(new java.math.BigDecimal("500000.00"));
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
    }

    @Test
    void testRequiredSourceSucceedsOptionalFails_Verified() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);

        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "FAILED", "Unavailable", null);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "SOURCE_UNAVAILABLE".equals(r.getCode()) && "EMPLOYMENT_SYSTEM".equals(r.getSource())));
    }
    
    @Test
    void testMissingRequiredField_Partial() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha"); // missing highestDegree
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);

        VerificationResult res = verificationService.verify(List.of(edu), policy);
        assertEquals(VerificationStatus.PARTIALLY_VERIFIED, res.getOverallStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "DATA_NOT_PROVIDED".equals(r.getCode()) && "highestDegree".equals(r.getFieldName())));
    }

    @Test
    void testMissingOptionalField_DoesNotFailVerification() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);
        
        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setFullName("Aditya Jha");
        // missing annualFamilyIncome (optional)
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);

        VerificationResult res = verificationService.verify(List.of(edu, emp), policy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "DATA_NOT_PROVIDED".equals(r.getCode()) && "annualFamilyIncome".equals(r.getFieldName())));
    }

    @Test
    void testNumericEquivalenceAndInvalidFormat() {
        ResolvedConsentPolicy customPolicy = new ResolvedConsentPolicy("TEST", Set.of(DataScope.EMPLOYMENT), "test", "employment", "DEPT", Set.of("annualFamilyIncome"), Set.of());
        
        CanonicalCitizenData empData1 = new CanonicalCitizenData();
        empData1.setAnnualFamilyIncome(new java.math.BigDecimal("500000.00"));
        SourceDataResult emp1 = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData1);
        
        VerificationResult res = verificationService.verify(List.of(emp1), customPolicy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
    }
    
    @Test
    void testDateStrictValidation() {
        ResolvedConsentPolicy customPolicy = new ResolvedConsentPolicy("TEST", Set.of(DataScope.EDUCATION), "test", "education", "DEPT", Set.of("dateOfBirth"), Set.of());
        
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setDateOfBirth("31-02-2020"); // Invalid date
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);
        
        VerificationResult res = verificationService.verify(List.of(edu), customPolicy);
        assertEquals(VerificationStatus.PARTIALLY_VERIFIED, res.getOverallStatus()); 
        assertTrue(res.getReasons().stream().anyMatch(r -> "INVALID_FORMAT".equals(r.getCode()) && "dateOfBirth".equals(r.getFieldName())));
        
        CanonicalCitizenData eduData2 = new CanonicalCitizenData();
        eduData2.setDateOfBirth("2020-02-29"); // Valid leap year
        SourceDataResult edu2 = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData2);
        
        res = verificationService.verify(List.of(edu2), customPolicy);
        assertEquals(VerificationStatus.VERIFIED, res.getOverallStatus());
    }

    @Test
    void testUnknownStatusHandledSafely() {
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "GARBAGE", null, null);
        VerificationResult res = verificationService.verify(List.of(edu), policy);
        
        assertEquals(VerificationStatus.UNABLE_TO_VERIFY, res.getOverallStatus());
        assertEquals("UNKNOWN_STATUS", res.getSourceResults().get(0).getStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "INVALID_SOURCE_DATA".equals(r.getCode()) && "EDUCATION_SYSTEM".equals(r.getSource())));
    }
    
    @Test
    void testDuplicateSourceResultSafelyRejected() {
        SourceDataResult edu1 = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, new CanonicalCitizenData());
        SourceDataResult edu2 = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, new CanonicalCitizenData());
        
        VerificationResult res = verificationService.verify(List.of(edu1, edu2), policy);
        assertEquals("FAILED", res.getSourceResults().get(0).getStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "INVALID_SOURCE_DATA".equals(r.getCode()) && "EDUCATION_SYSTEM".equals(r.getSource())));
    }
    
    @Test
    void testSimultaneousConflictAndSourceFailure() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);
        
        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setFullName("Aditya Jah"); // Conflict
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);
        
        SourceDataResult health = createSourceResult("HEALTH_SYSTEM", "FAILED", "Timeout", null);
        
        VerificationResult res = verificationService.verify(List.of(edu, emp, health), policy);
        
        assertEquals(VerificationStatus.NOT_VERIFIED, res.getOverallStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "DATA_CONFLICT".equals(r.getCode()) && "fullName".equals(r.getFieldName())));
        assertTrue(res.getReasons().stream().anyMatch(r -> "SOURCE_UNAVAILABLE".equals(r.getCode()) && "HEALTH_SYSTEM".equals(r.getSource())));
    }

    @Test
    void testDeterminism() {
        CanonicalCitizenData eduData = new CanonicalCitizenData();
        eduData.setFullName("Aditya Jha");
        eduData.setHighestDegree("B.Tech");
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);
        
        CanonicalCitizenData empData = new CanonicalCitizenData();
        empData.setFullName("Aditya Jah"); // Conflict
        SourceDataResult emp = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, empData);
        
        List<SourceDataResult> list1 = List.of(edu, emp);
        List<SourceDataResult> list2 = List.of(emp, edu);
        
        VerificationResult res1 = verificationService.verify(list1, policy);
        VerificationResult res2 = verificationService.verify(list2, policy);
        
        assertEquals(res1.getOverallStatus(), res2.getOverallStatus());
        assertEquals(res1.getReasons().size(), res2.getReasons().size());
        assertEquals(res1.getFieldResults().size(), res2.getFieldResults().size());
    }
    
    @Test
    void testInvalidFollowedByValid() {
        ResolvedConsentPolicy customPolicy = new ResolvedConsentPolicy("TEST", Set.of(DataScope.EDUCATION), "test", "education", "DEPT", Set.of("dateOfBirth"), Set.of());
        
        CanonicalCitizenData eduData1 = new CanonicalCitizenData();
        eduData1.setDateOfBirth("31-02-2020"); // Invalid
        SourceDataResult edu1 = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData1);
        
        CanonicalCitizenData eduData2 = new CanonicalCitizenData();
        eduData2.setDateOfBirth("2020-02-29"); // Valid
        SourceDataResult edu2 = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, eduData2);
        
        VerificationResult res = verificationService.verify(List.of(edu1, edu2), customPolicy);
        
        assertEquals(VerificationStatus.PARTIALLY_VERIFIED, res.getOverallStatus()); 
        
        FieldVerificationResult fRes = res.getFieldResults().get(0);
        assertEquals(FieldVerificationStatus.UNABLE_TO_VERIFY, fRes.getStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "INVALID_FORMAT".equals(r.getCode()) && "EDUCATION_SYSTEM".equals(r.getSource())));
    }
    
    @Test
    void testValidInvalidValidOrderIndependence() {
        ResolvedConsentPolicy customPolicy = new ResolvedConsentPolicy("TEST", Set.of(DataScope.EDUCATION), "test", "education", "DEPT", Set.of("dateOfBirth"), Set.of());
        
        CanonicalCitizenData dataA = new CanonicalCitizenData();
        dataA.setDateOfBirth("2020-02-29");
        SourceDataResult srcA = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, dataA);
        
        CanonicalCitizenData dataB = new CanonicalCitizenData();
        dataB.setDateOfBirth("31-02-2020");
        SourceDataResult srcB = createSourceResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, dataB);
        
        CanonicalCitizenData dataC = new CanonicalCitizenData();
        dataC.setDateOfBirth("2020-02-29");
        SourceDataResult srcC = createSourceResult("HEALTH_SYSTEM", "SUCCESS", null, dataC);
        
        VerificationResult res1 = verificationService.verify(List.of(srcA, srcB, srcC), customPolicy);
        VerificationResult res2 = verificationService.verify(List.of(srcC, srcB, srcA), customPolicy);
        
        assertEquals(res1.getOverallStatus(), res2.getOverallStatus());
        assertEquals(FieldVerificationStatus.UNABLE_TO_VERIFY, res1.getFieldResults().get(0).getStatus());
        assertTrue(res1.getReasons().stream().anyMatch(r -> "INVALID_FORMAT".equals(r.getCode()) && "EMPLOYMENT_SYSTEM".equals(r.getSource())));
    }
    
    @Test
    void testSourceUnavailableVsDataNotProvided() {
        // Source fails
        SourceDataResult edu1 = createSourceResult("EDUCATION_SYSTEM", "FAILED", "Error", null);
        VerificationResult res1 = verificationService.verify(List.of(edu1), policy);
        assertTrue(res1.getReasons().stream().anyMatch(r -> "SOURCE_UNAVAILABLE".equals(r.getCode()) && "EDUCATION_SYSTEM".equals(r.getSource())));
        assertFalse(res1.getReasons().stream().anyMatch(r -> "DATA_NOT_PROVIDED".equals(r.getCode()) && "EDUCATION_SYSTEM".equals(r.getSource())));
        
        // Source succeeds, data missing
        CanonicalCitizenData data2 = new CanonicalCitizenData();
        data2.setFullName("Aditya"); // highestDegree is missing
        SourceDataResult edu2 = createSourceResult("EDUCATION_SYSTEM", "SUCCESS", null, data2);
        VerificationResult res2 = verificationService.verify(List.of(edu2), policy);
        assertTrue(res2.getReasons().stream().anyMatch(r -> "DATA_NOT_PROVIDED".equals(r.getCode()) && "highestDegree".equals(r.getFieldName())));
    }
    
    @Test
    void testRequiredSourceUnavailable() {
        SourceDataResult edu = createSourceResult("EDUCATION_SYSTEM", "FAILED", "Error", null); // Required source
        VerificationResult res = verificationService.verify(List.of(edu), policy);
        assertEquals(VerificationStatus.UNABLE_TO_VERIFY, res.getOverallStatus());
        assertTrue(res.getReasons().stream().anyMatch(r -> "SOURCE_UNAVAILABLE".equals(r.getCode()) && "EDUCATION_SYSTEM".equals(r.getSource())));
    }
}
