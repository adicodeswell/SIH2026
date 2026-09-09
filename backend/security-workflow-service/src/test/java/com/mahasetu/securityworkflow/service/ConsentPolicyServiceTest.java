package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.config.ConsentPolicyProperties;
import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import com.mahasetu.securityworkflow.dto.DataScope;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.exception.UnsupportedServiceCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConsentPolicyServiceTest {

    private ConsentPolicyService consentPolicyService;
    private ConsentPolicyProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ConsentPolicyProperties();
        properties.setPolicies(Map.of(
                "SKILL_BENEFIT", new ConsentPolicy("education,employment,skills", "verification"),
                "SCHOLARSHIP", new ConsentPolicy("education", "scholarship_verification"),
                "SRV-EDU", new ConsentPolicy("education,health", "verification")
        ));
        consentPolicyService = new ConsentPolicyService(properties);
        consentPolicyService.validateAndInitializePolicies();
    }

    @Test
    void testGetPolicy_SkillBenefit_ResolvesCorrectly() {
        ResolvedConsentPolicy policy = consentPolicyService.getPolicy("SKILL_BENEFIT");
        assertNotNull(policy);
        
        Set<DataScope> expectedScopes = Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT, DataScope.SKILLS);
        assertEquals(expectedScopes, policy.getRequiredScopes());
        assertEquals("verification", policy.getPurpose());
        assertEquals("SKILL_BENEFIT", policy.getServiceCode());
    }

    @Test
    void testGetPolicy_Scholarship_ResolvesCorrectly() {
        ResolvedConsentPolicy scholarship = consentPolicyService.getPolicy("SCHOLARSHIP");
        assertNotNull(scholarship);
        
        Set<DataScope> expectedScopes = Set.of(DataScope.EDUCATION);
        assertEquals(expectedScopes, scholarship.getRequiredScopes());
        assertEquals("scholarship_verification", scholarship.getPurpose());
    }

    @Test
    void testGetPolicy_UnknownServiceCode_ThrowsUnsupportedServiceCodeException() {
        UnsupportedServiceCodeException exception = assertThrows(
                UnsupportedServiceCodeException.class,
                () -> consentPolicyService.getPolicy("UNKNOWN_SERVICE")
        );
        assertEquals("UNKNOWN_SERVICE", exception.getServiceCode());
        assertTrue(exception.getMessage().contains("UNKNOWN_SERVICE"));
    }

    @Test
    void testInit_InvalidScope_ThrowsException() {
        ConsentPolicyProperties badProps = new ConsentPolicyProperties();
        badProps.setPolicies(Map.of(
                "BAD_SERVICE", new ConsentPolicy("education,everything", "verification")
        ));
        ConsentPolicyService badService = new ConsentPolicyService(badProps);
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                badService::validateAndInitializePolicies
        );
        assertTrue(exception.getMessage().contains("everything"));
    }

    @Test
    void testInit_DuplicateScopes_ResolvedSafely() {
        ConsentPolicyProperties dupProps = new ConsentPolicyProperties();
        dupProps.setPolicies(Map.of(
                "DUP_SERVICE", new ConsentPolicy("education,education, employment", "verification")
        ));
        ConsentPolicyService dupService = new ConsentPolicyService(dupProps);
        dupService.validateAndInitializePolicies();
        
        ResolvedConsentPolicy policy = dupService.getPolicy("DUP_SERVICE");
        Set<DataScope> expectedScopes = Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT);
        assertEquals(expectedScopes, policy.getRequiredScopes());
    }
    
    @Test
    void testInit_Normalization_HandlesWhitespaceAndCase() {
        ConsentPolicyProperties normProps = new ConsentPolicyProperties();
        normProps.setPolicies(Map.of(
                "NORM_SERVICE", new ConsentPolicy(" EDUCATION , Employment , skills ", "verification")
        ));
        ConsentPolicyService normService = new ConsentPolicyService(normProps);
        normService.validateAndInitializePolicies();
        
        ResolvedConsentPolicy policy = normService.getPolicy("NORM_SERVICE");
        Set<DataScope> expectedScopes = Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT, DataScope.SKILLS);
        
        // Verify exactly the expected scopes (no duplicates, no missing)
        assertEquals(3, policy.getRequiredScopes().size());
        assertEquals(expectedScopes, policy.getRequiredScopes());
    }

    @Test
    void testInit_EmptyScope_ThrowsException() {
        // Test empty string
        ConsentPolicyProperties emptyProps = new ConsentPolicyProperties();
        emptyProps.setPolicies(Map.of(
                "EMPTY_SERVICE", new ConsentPolicy("", "verification")
        ));
        ConsentPolicyService emptyService = new ConsentPolicyService(emptyProps);
        
        IllegalStateException exceptionEmpty = assertThrows(
                IllegalStateException.class,
                emptyService::validateAndInitializePolicies
        );
        assertTrue(exceptionEmpty.getMessage().contains("empty or blank"));

        // Test whitespace only string
        ConsentPolicyProperties blankProps = new ConsentPolicyProperties();
        blankProps.setPolicies(Map.of(
                "BLANK_SERVICE", new ConsentPolicy("   ", "verification")
        ));
        ConsentPolicyService blankService = new ConsentPolicyService(blankProps);
        
        IllegalStateException exceptionBlank = assertThrows(
                IllegalStateException.class,
                blankService::validateAndInitializePolicies
        );
        assertTrue(exceptionBlank.getMessage().contains("empty or blank"));
        
        // Test commas only string
        ConsentPolicyProperties commaProps = new ConsentPolicyProperties();
        commaProps.setPolicies(Map.of(
                "COMMA_SERVICE", new ConsentPolicy(",,,", "verification")
        ));
        ConsentPolicyService commaService = new ConsentPolicyService(commaProps);
        
        IllegalStateException exceptionComma = assertThrows(
                IllegalStateException.class,
                commaService::validateAndInitializePolicies
        );
        assertTrue(exceptionComma.getMessage().contains("must contain at least one valid scope"));
    }

    @Test
    void testGetPolicy_NullOrEmptyCode_ThrowsUnsupportedServiceCodeException() {
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy(null));
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy(""));
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy("   "));
    }
    
    @Test
    void testPolicyImmutability() {
        ResolvedConsentPolicy policy = consentPolicyService.getPolicy("SCHOLARSHIP");
        Set<DataScope> scopes = policy.getRequiredScopes();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            scopes.add(DataScope.HEALTH);
        });
    }
}
