package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.config.ConsentPolicyProperties;
import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import com.mahasetu.securityworkflow.dto.DataScope;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.exception.UnsupportedServiceCodeException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConsentPolicyServiceTest {

    @Test
    void testValidateAndInitializePolicies_ValidConfig_Succeeds() {
        ConsentPolicyProperties props = new ConsentPolicyProperties();
        props.setPolicies(Map.of(
                "SKILL_BENEFIT", new ConsentPolicy("education,employment,skills", "verification", "DEPT-SKILLS"),
                "SCHOLARSHIP", new ConsentPolicy("education", "scholarship", "DEPT-EDU")
        ));

        ConsentPolicyService service = new ConsentPolicyService(props);
        service.validateAndInitializePolicies();

        ResolvedConsentPolicy skillPolicy = service.getPolicy("SKILL_BENEFIT");
        assertEquals("verification", skillPolicy.getPurpose());
        assertEquals("DEPT-SKILLS", skillPolicy.getRequestingDepartmentId());
        assertTrue(skillPolicy.getRequiredScopes().contains(DataScope.EDUCATION));
        assertTrue(skillPolicy.getRequiredScopes().contains(DataScope.EMPLOYMENT));
        assertTrue(skillPolicy.getRequiredScopes().contains(DataScope.SKILLS));

        ResolvedConsentPolicy eduPolicy = service.getPolicy("SCHOLARSHIP");
        assertEquals("scholarship", eduPolicy.getPurpose());
        assertEquals("DEPT-EDU", eduPolicy.getRequestingDepartmentId());
        assertTrue(eduPolicy.getRequiredScopes().contains(DataScope.EDUCATION));
    }

    @Test
    void testValidateAndInitializePolicies_InvalidScope_ThrowsException() {
        ConsentPolicyProperties props = new ConsentPolicyProperties();
        props.setPolicies(Map.of(
                "TEST", new ConsentPolicy("education,INVALID_SCOPE", "verification", "DEPT-TEST")
        ));

        ConsentPolicyService service = new ConsentPolicyService(props);
        assertThrows(IllegalStateException.class, service::validateAndInitializePolicies);
    }

    @Test
    void testValidateAndInitializePolicies_MissingDepartment_ThrowsException() {
        ConsentPolicyProperties props = new ConsentPolicyProperties();
        props.setPolicies(Map.of(
                "TEST", new ConsentPolicy("education", "verification", "")
        ));

        ConsentPolicyService service = new ConsentPolicyService(props);
        assertThrows(IllegalStateException.class, service::validateAndInitializePolicies);
    }

    @Test
    void testGetPolicy_UnknownService_ThrowsUnsupportedServiceCodeException() {
        ConsentPolicyProperties props = new ConsentPolicyProperties();
        props.setPolicies(Map.of(
                "SKILL_BENEFIT", new ConsentPolicy("education", "verification", "DEPT-TEST")
        ));

        ConsentPolicyService service = new ConsentPolicyService(props);
        service.validateAndInitializePolicies();

        assertThrows(UnsupportedServiceCodeException.class, () -> service.getPolicy("UNKNOWN"));
    }
}
