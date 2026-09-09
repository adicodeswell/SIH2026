package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.config.ConsentPolicyProperties;
import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.exception.ValidationException;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsentHardeningTest {

    private ConsentRepository consentRepository;
    private AuditService auditService;
    private ConsentPolicyService consentPolicyService;
    private com.mahasetu.securityworkflow.client.ApplicationServiceClient applicationServiceClient;
    private ConsentService consentService;

    private static final String CITIZEN_1 = "C123";

    @BeforeEach
    void setUp() {
        consentRepository = mock(ConsentRepository.class);
        auditService = mock(AuditService.class);
        applicationServiceClient = mock(com.mahasetu.securityworkflow.client.ApplicationServiceClient.class);

        ConsentPolicyProperties props = new ConsentPolicyProperties();
        ConsentPolicy eduPolicy = new ConsentPolicy();
        eduPolicy.setDataScope("education");
        eduPolicy.setPurpose("verification");
        eduPolicy.setRequestingDepartmentId("DEPT-EDU");
        props.setPolicies(Map.of("SRV-EDU", eduPolicy));

        consentPolicyService = new ConsentPolicyService(props);
        consentPolicyService.validateAndInitializePolicies();

        consentService = new ConsentService(consentRepository, auditService, consentPolicyService, applicationServiceClient);
    }

    @Test
    void testGrantConsent_ValidationErrors() {
        ConsentRequest validReq = new ConsentRequest();
        validReq.setApplicationId("A001");
        validReq.setServiceCode("SRV-EDU");

        // Blank citizenId
        assertThrows(ValidationException.class, () -> consentService.grantConsent("", validReq));
        assertThrows(ValidationException.class, () -> consentService.grantConsent(null, validReq));

        // Null request
        assertThrows(ValidationException.class, () -> consentService.grantConsent(CITIZEN_1, null));

        // Blank applicationId
        ConsentRequest invalidApp = new ConsentRequest();
        invalidApp.setApplicationId("   ");
        invalidApp.setServiceCode("SRV-EDU");
        assertThrows(ValidationException.class, () -> consentService.grantConsent(CITIZEN_1, invalidApp));

        // Blank serviceCode
        ConsentRequest invalidCode = new ConsentRequest();
        invalidCode.setApplicationId("A001");
        invalidCode.setServiceCode("");
        assertThrows(ValidationException.class, () -> consentService.grantConsent(CITIZEN_1, invalidCode));
    }

    @Test
    void testRevokeConsent_RequiresOwnership() {
        UUID consentId = UUID.randomUUID();
        Consent consent = new Consent();
        consent.setId(consentId);
        consent.setCitizenId(CITIZEN_1);
        consent.setStatus("GRANTED");

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        // Attempt revoke with wrong citizenId
        assertThrows(SecurityException.class, () -> consentService.revokeConsent("HACKER", consentId));
    }

    @Test
    void testConsentPolicy_KnownServiceCode_ReturnsPolicy() {
        ResolvedConsentPolicy policy = consentPolicyService.getPolicy("SRV-EDU");
        assertNotNull(policy);
        assertEquals("education", policy.getRawDataScope());
        assertEquals("verification", policy.getPurpose());
        assertEquals("DEPT-EDU", policy.getRequestingDepartmentId());
    }
}
