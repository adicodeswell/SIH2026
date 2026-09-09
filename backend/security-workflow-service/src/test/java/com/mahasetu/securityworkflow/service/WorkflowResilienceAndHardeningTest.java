package com.mahasetu.securityworkflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.client.InteroperabilityClient;
import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.exception.ValidationException;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import com.mahasetu.securityworkflow.service.worker.InteroperabilityWorker;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowResilienceAndHardeningTest {

    @Mock
    private InteroperabilityClient interoperabilityClient;
    @Mock
    private DelegateExecution execution;
    @Mock
    private ConsentRepository consentRepository;
    @Mock
    private AuditService auditService;

    private InteroperabilityWorker interopWorker;
    private ConsentService consentService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        interopWorker = new InteroperabilityWorker(interoperabilityClient, objectMapper);
        consentService = new ConsentService(consentRepository, auditService);
    }

    // =========================================================================
    // 1. Interoperability Failure & Resilience Tests
    // =========================================================================

    @Test
    void testInteropWorker_Downstream500_PreservesFailureReasonAndThrowsBpmnError() {
        when(execution.getVariable("citizenId")).thenReturn("CIT-ERR500");
        when(execution.getVariable("applicationId")).thenReturn("APP-ERR500");

        when(interoperabilityClient.fetchAllData("CIT-ERR500"))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error", HttpHeaders.EMPTY, null, null));

        BpmnError error = assertThrows(BpmnError.class, () -> interopWorker.execute(execution));

        assertEquals("INTEROP_FETCH_FAILED", error.getErrorCode());
        verify(execution).setVariable(eq("failureReason"), argThat(arg ->
                arg != null && arg.toString().contains("Interoperability downstream server error (500 INTERNAL_SERVER_ERROR)")));
    }

    @Test
    void testInteropWorker_Downstream404_PreservesFailureReasonAndThrowsBpmnError() {
        when(execution.getVariable("citizenId")).thenReturn("CIT-ERR404");
        when(execution.getVariable("applicationId")).thenReturn("APP-ERR404");

        when(interoperabilityClient.fetchAllData("CIT-ERR404"))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND,
                        "Not Found", HttpHeaders.EMPTY, null, null));

        BpmnError error = assertThrows(BpmnError.class, () -> interopWorker.execute(execution));

        assertEquals("INTEROP_FETCH_FAILED", error.getErrorCode());
        verify(execution).setVariable(eq("failureReason"), argThat(arg ->
                arg != null && arg.toString().contains("Interoperability client error (404 NOT_FOUND)")));
    }

    @Test
    void testInteropWorker_EmptyDataList_ThrowsBpmnErrorAndSetsReason() {
        when(execution.getVariable("citizenId")).thenReturn("CIT-EMPTY");
        when(execution.getVariable("applicationId")).thenReturn("APP-EMPTY");

        when(interoperabilityClient.fetchAllData("CIT-EMPTY")).thenReturn(Collections.emptyList());

        BpmnError error = assertThrows(BpmnError.class, () -> interopWorker.execute(execution));

        assertEquals("INTEROP_FETCH_FAILED", error.getErrorCode());
        verify(execution).setVariable("failureReason", "Citizen record not found in Interoperability Service");
    }

    @Test
    void testInteropWorker_Success_SetsVariables() throws Exception {
        when(execution.getVariable("citizenId")).thenReturn("CIT-OK");
        when(execution.getVariable("applicationId")).thenReturn("APP-OK");

        CanonicalCitizenData record = new CanonicalCitizenData();
        record.setCitizenId("CIT-OK");
        record.setFullName("Ramesh Patil");

        when(interoperabilityClient.fetchAllData("CIT-OK")).thenReturn(List.of(record));

        interopWorker.execute(execution);

        verify(execution).setVariable(eq("workflowStatus"), eq("SUCCESS"));
        verify(execution).setVariable(eq("interoperabilityResult"), argThat(arg ->
                arg != null && arg.toString().contains("Ramesh Patil")));
    }

    // =========================================================================
    // 2. Consent Hardening & Default-Deny Tests
    // =========================================================================

    @Test
    void testConsent_ExpiredConsent_DefaultDeny() {
        Consent c = new Consent();
        c.setExpiresAt(LocalDateTime.now().minusHours(2));

        when(consentRepository.findFirstByCitizenIdAndDataScopeAndPurposeAndStatusOrderByGrantedAtDesc(
                "CIT-EXPIRED", "education", "verification", "GRANTED")).thenReturn(Optional.of(c));

        assertFalse(consentService.checkConsent("CIT-EXPIRED", "education", "verification"));
    }

    @Test
    void testConsent_RevokedConsent_DefaultDeny() {
        when(consentRepository.findFirstByCitizenIdAndDataScopeAndPurposeAndStatusOrderByGrantedAtDesc(
                "CIT-REVOKED", "education", "verification", "GRANTED")).thenReturn(Optional.empty());

        assertFalse(consentService.checkConsent("CIT-REVOKED", "education", "verification"));
    }

    @Test
    void testConsent_Grant_ValidationAndAudit() {
        ConsentRequest req = new ConsentRequest();
        req.setDataScope("skills");
        req.setPurpose("job_verification");
        req.setRequestingDepartmentId("DEPT-SKILL");

        when(consentRepository.save(any(Consent.class))).thenAnswer(inv -> {
            Consent consent = inv.getArgument(0);
            consent.setId(UUID.randomUUID());
            return consent;
        });

        Consent saved = consentService.grantConsent("CIT-USER1", req);

        assertNotNull(saved);
        assertEquals("GRANTED", saved.getStatus());
        verify(auditService).recordConsentGranted(eq("CIT-USER1"), eq(saved.getId()),
                eq("DEPT-SKILL"), eq("skills"), eq("job_verification"));
    }

    @Test
    void testConsent_Grant_MissingDepartment_ThrowsValidationException() {
        ConsentRequest req = new ConsentRequest();
        req.setDataScope("skills");
        req.setPurpose("job_verification");
        req.setRequestingDepartmentId("");

        assertThrows(ValidationException.class, () -> consentService.grantConsent("CIT-USER1", req));
        verify(consentRepository, never()).save(any());
        verify(auditService, never()).recordConsentGranted(any(), any(), any(), any(), any());
    }

    @Test
    void testConsent_Revoke_IdempotentOnSubsequentCalls() {
        UUID consentId = UUID.randomUUID();
        Consent consent = new Consent();
        consent.setId(consentId);
        consent.setCitizenId("CIT-USER1");
        consent.setStatus("REVOKED");

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        consentService.revokeConsent("CIT-USER1", consentId);

        assertEquals("REVOKED", consent.getStatus());
        verify(consentRepository).save(consent);
        // On subsequent revoke of already revoked consent, audit record not duplicated
        verify(auditService, never()).recordConsentRevoked(any(), any());
    }

    @Test
    void testConsent_Revoke_UnauthorizedCitizen_ThrowsSecurityException() {
        UUID consentId = UUID.randomUUID();
        Consent consent = new Consent();
        consent.setId(consentId);
        consent.setCitizenId("CIT-ORIGINAL");

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        assertThrows(SecurityException.class,
                () -> consentService.revokeConsent("CIT-IMPOSTER", consentId));
    }
}
