package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsentServiceTest {

    private ConsentRepository consentRepository;
    private AuditService auditService;
    private ConsentService consentService;

    @BeforeEach
    void setUp() {
        consentRepository = mock(ConsentRepository.class);
        auditService = mock(AuditService.class);
        consentService = new ConsentService(consentRepository, auditService);
    }

    @Test
    void testGrantConsent_Success() {
        ConsentRequest req = new ConsentRequest();
        req.setDataScope("HEALTH");
        req.setPurpose("VERIFICATION");
        req.setRequestingDepartmentId("DEP1");

        when(consentRepository.save(any(Consent.class))).thenAnswer(i -> {
            Consent c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Consent c = consentService.grantConsent("cit-123", req);
        assertNotNull(c.getId());
        assertEquals("cit-123", c.getCitizenId());
        assertEquals("HEALTH", c.getDataScope());
        assertEquals("GRANTED", c.getStatus());
        verify(auditService).recordConsentGranted(eq("cit-123"), eq(c.getId()), eq("DEP1"), eq("HEALTH"), eq("VERIFICATION"));
    }

    @Test
    void testGrantConsent_ValidationFailures() {
        ConsentRequest req = new ConsentRequest();
        assertThrows(com.mahasetu.securityworkflow.exception.ValidationException.class,
                () -> consentService.grantConsent(null, req));

        assertThrows(com.mahasetu.securityworkflow.exception.ValidationException.class,
                () -> consentService.grantConsent("cit-123", null));

        req.setDataScope("");
        assertThrows(com.mahasetu.securityworkflow.exception.ValidationException.class,
                () -> consentService.grantConsent("cit-123", req));

        req.setDataScope("HEALTH");
        req.setPurpose("");
        assertThrows(com.mahasetu.securityworkflow.exception.ValidationException.class,
                () -> consentService.grantConsent("cit-123", req));

        req.setPurpose("VERIFICATION");
        req.setRequestingDepartmentId(" ");
        assertThrows(com.mahasetu.securityworkflow.exception.ValidationException.class,
                () -> consentService.grantConsent("cit-123", req));
    }

    @Test
    void testRevokeConsent_SuccessAndAudit() {
        Consent c = new Consent();
        c.setId(UUID.randomUUID());
        c.setCitizenId("cit-123");
        c.setStatus("GRANTED");

        when(consentRepository.findById(c.getId())).thenReturn(Optional.of(c));

        consentService.revokeConsent("cit-123", c.getId());

        assertEquals("REVOKED", c.getStatus());
        verify(consentRepository).save(c);
        verify(auditService).recordConsentRevoked("cit-123", c.getId());
    }

    @Test
    void testRevokeConsent_IdempotentOnAlreadyRevoked() {
        Consent c = new Consent();
        UUID id = UUID.randomUUID();
        c.setId(id);
        c.setCitizenId("cit-123");
        c.setStatus("REVOKED");

        when(consentRepository.findById(id)).thenReturn(Optional.of(c));

        consentService.revokeConsent("cit-123", id);

        assertEquals("REVOKED", c.getStatus());
        verify(consentRepository).save(c);
    }

    @Test
    void testRevokeConsent_WrongCitizen() {
        Consent c = new Consent();
        c.setId(UUID.randomUUID());
        c.setCitizenId("cit-other");
        c.setStatus("GRANTED");

        when(consentRepository.findById(c.getId())).thenReturn(Optional.of(c));

        assertThrows(SecurityException.class, () -> consentService.revokeConsent("cit-123", c.getId()));
    }

    @Test
    void testCheckConsent_Valid() {
        Consent c = new Consent();
        c.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(consentRepository.findByCitizenIdAndDataScopeAndPurposeAndStatus(
                "cit-1", "scope", "purpose", "GRANTED")).thenReturn(Optional.of(c));

        assertTrue(consentService.checkConsent("cit-1", "scope", "purpose"));
    }

    @Test
    void testCheckConsent_Expired() {
        Consent c = new Consent();
        c.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(consentRepository.findByCitizenIdAndDataScopeAndPurposeAndStatus(
                "cit-1", "scope", "purpose", "GRANTED")).thenReturn(Optional.of(c));

        assertFalse(consentService.checkConsent("cit-1", "scope", "purpose"));
    }

    @Test
    void testCheckConsent_DefaultDeny_NotFound() {
        when(consentRepository.findByCitizenIdAndDataScopeAndPurposeAndStatus(
                "unknown", "scope", "purpose", "GRANTED")).thenReturn(Optional.empty());

        assertFalse(consentService.checkConsent("unknown", "scope", "purpose"));
    }
}
