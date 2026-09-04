package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.config.ConsentPolicyProperties;
import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import com.mahasetu.securityworkflow.exception.UnsupportedServiceCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
                "SRV-EDU", new ConsentPolicy("education", "verification")
        ));
        consentPolicyService = new ConsentPolicyService(properties);
    }

    @Test
    void testGetPolicy_KnownServiceCode_ReturnsPolicy() {
        ConsentPolicy policy = consentPolicyService.getPolicy("SKILL_BENEFIT");
        assertNotNull(policy);
        assertEquals("education,employment,skills", policy.getDataScope());
        assertEquals("verification", policy.getPurpose());
    }

    @Test
    void testGetPolicy_DifferentKnownCodes_ReturnRespectivePolicies() {
        ConsentPolicy scholarship = consentPolicyService.getPolicy("SCHOLARSHIP");
        assertNotNull(scholarship);
        assertEquals("education", scholarship.getDataScope());
        assertEquals("scholarship_verification", scholarship.getPurpose());

        ConsentPolicy srvEdu = consentPolicyService.getPolicy("SRV-EDU");
        assertNotNull(srvEdu);
        assertEquals("education", srvEdu.getDataScope());
        assertEquals("verification", srvEdu.getPurpose());
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
    void testGetPolicy_NullOrEmptyCode_ThrowsUnsupportedServiceCodeException() {
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy(null));
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy(""));
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy("   "));
    }
}
