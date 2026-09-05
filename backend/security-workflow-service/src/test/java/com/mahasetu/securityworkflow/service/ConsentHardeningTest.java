package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.config.ConsentPolicyProperties;
import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.exception.UnsupportedServiceCodeException;
import com.mahasetu.securityworkflow.exception.ValidationException;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsentHardeningTest {

    @Mock
    private ConsentRepository consentRepository;

    @Mock
    private AuditService auditService;

    private ConsentService consentService;
    private ConsentPolicyService consentPolicyService;

    private static final String CITIZEN_1 = "CIT-100";
    private static final String CITIZEN_2 = "CIT-200";
    private static final UUID CONSENT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        consentService = new ConsentService(consentRepository, auditService);

        ConsentPolicyProperties props = new ConsentPolicyProperties();
        ConsentPolicy eduPolicy = new ConsentPolicy();
        eduPolicy.setDataScope("education");
        eduPolicy.setPurpose("verification");
        props.setPolicies(Map.of("SRV-EDU", eduPolicy));

        consentPolicyService = new ConsentPolicyService(props);
    }

    @Test
    void testGrantConsent_ValidationErrors() {
        ConsentRequest validReq = new ConsentRequest();
        validReq.setDataScope("education");
        validReq.setPurpose("verification");
        validReq.setRequestingDepartmentId("DEPT-1");

        // Blank citizenId
        assertThrows(ValidationException.class, () -> consentService.grantConsent("", validReq));
        assertThrows(ValidationException.class, () -> consentService.grantConsent(null, validReq));

        // Null request
        assertThrows(ValidationException.class, () -> consentService.grantConsent(CITIZEN_1, null));

        // Blank dataScope
        ConsentRequest invalidScope = new ConsentRequest();
        invalidScope.setDataScope("   ");
        invalidScope.setPurpose("verification");
        invalidScope.setRequestingDepartmentId("DEPT-1");
        assertThrows(ValidationException.class, () -> consentService.grantConsent(CITIZEN_1, invalidScope));

        // Blank purpose
        ConsentRequest invalidPurpose = new ConsentRequest();
        invalidPurpose.setDataScope("education");
        invalidPurpose.setPurpose("");
        invalidPurpose.setRequestingDepartmentId("DEPT-1");
        assertThrows(ValidationException.class, () -> consentService.grantConsent(CITIZEN_1, invalidPurpose));

        // Blank requestingDepartmentId
        ConsentRequest invalidDept = new ConsentRequest();
        invalidDept.setDataScope("education");
        invalidDept.setPurpose("verification");
        invalidDept.setRequestingDepartmentId(null);
        assertThrows(ValidationException.class, () -> consentService.grantConsent(CITIZEN_1, invalidDept));
    }

    @Test
    void testGrantConsent_Success_RecordsAudit() {
        ConsentRequest req = new ConsentRequest();
        req.setDataScope("education");
        req.setPurpose("verification");
        req.setRequestingDepartmentId("DEPT-1");

        Consent mockSaved = new Consent();
        mockSaved.setId(CONSENT_ID);
        mockSaved.setCitizenId(CITIZEN_1);
        mockSaved.setDataScope("education");
        mockSaved.setPurpose("verification");
        mockSaved.setRequestingDepartmentId("DEPT-1");
        mockSaved.setStatus("GRANTED");

        when(consentRepository.save(any(Consent.class))).thenReturn(mockSaved);

        Consent result = consentService.grantConsent(CITIZEN_1, req);

        assertNotNull(result);
        assertEquals("GRANTED", result.getStatus());
        verify(auditService).recordConsentGranted(eq(CITIZEN_1), eq(CONSENT_ID), eq("DEPT-1"), eq("education"), eq("verification"));
    }

    @Test
    void testRevokeConsent_OwnConsent_SuccessAndAudit() {
        Consent consent = new Consent();
        consent.setId(CONSENT_ID);
        consent.setCitizenId(CITIZEN_1);
        consent.setStatus("GRANTED");

        when(consentRepository.findById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(consentRepository.save(any(Consent.class))).thenReturn(consent);

        consentService.revokeConsent(CITIZEN_1, CONSENT_ID);

        assertEquals("REVOKED", consent.getStatus());
        verify(auditService).recordConsentRevoked(CITIZEN_1, CONSENT_ID);
    }

    @Test
    void testRevokeConsent_RepeatedRevoke_IdempotentSafe() {
        Consent consent = new Consent();
        consent.setId(CONSENT_ID);
        consent.setCitizenId(CITIZEN_1);
        consent.setStatus("REVOKED");

        when(consentRepository.findById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(consentRepository.save(any(Consent.class))).thenReturn(consent);

        consentService.revokeConsent(CITIZEN_1, CONSENT_ID);

        assertEquals("REVOKED", consent.getStatus());
        // Verify audit log is NOT duplicated on repeated revoke
        verify(auditService, never()).recordConsentRevoked(anyString(), any(UUID.class));
    }

    @Test
    void testRevokeConsent_OtherCitizen_ThrowsSecurityException() {
        Consent consent = new Consent();
        consent.setId(CONSENT_ID);
        consent.setCitizenId(CITIZEN_1);
        consent.setStatus("GRANTED");

        when(consentRepository.findById(CONSENT_ID)).thenReturn(Optional.of(consent));

        assertThrows(SecurityException.class, () -> consentService.revokeConsent(CITIZEN_2, CONSENT_ID));
    }

    @Test
    void testCheckConsent_ExpiredConsent_ReturnsFalse() {
        Consent consent = new Consent();
        consent.setCitizenId(CITIZEN_1);
        consent.setDataScope("education");
        consent.setPurpose("verification");
        consent.setStatus("GRANTED");
        consent.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday

        when(consentRepository.findByCitizenIdAndDataScopeAndPurposeAndStatus(CITIZEN_1, "education", "verification", "GRANTED"))
                .thenReturn(Optional.of(consent));

        boolean valid = consentService.checkConsent(CITIZEN_1, "education", "verification");
        assertFalse(valid);
    }

    @Test
    void testCheckConsent_MissingOrWrongParameters_ReturnsFalse() {
        when(consentRepository.findByCitizenIdAndDataScopeAndPurposeAndStatus(anyString(), anyString(), anyString(), eq("GRANTED")))
                .thenReturn(Optional.empty());

        assertFalse(consentService.checkConsent(CITIZEN_1, "health", "verification"));
        assertFalse(consentService.checkConsent("UNKNOWN_CIT", "education", "verification"));
    }

    @Test
    void testConsentPolicy_UnknownServiceCode_DefaultDeny() {
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy("UNKNOWN_SERVICE"));
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy(null));
        assertThrows(UnsupportedServiceCodeException.class, () -> consentPolicyService.getPolicy("  "));
    }

    @Test
    void testConsentPolicy_KnownServiceCode_ReturnsPolicy() {
        ConsentPolicy policy = consentPolicyService.getPolicy("SRV-EDU");
        assertNotNull(policy);
        assertEquals("education", policy.getDataScope());
        assertEquals("verification", policy.getPurpose());
    }
}
