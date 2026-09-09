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
    private ConsentPolicyService consentPolicyService;
    private com.mahasetu.securityworkflow.client.ApplicationServiceClient applicationServiceClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        interopWorker = new InteroperabilityWorker(interoperabilityClient, objectMapper);
        consentPolicyService = mock(ConsentPolicyService.class);
        applicationServiceClient = mock(com.mahasetu.securityworkflow.client.ApplicationServiceClient.class);
        consentService = new ConsentService(consentRepository, auditService, consentPolicyService, applicationServiceClient);
    }

    // =========================================================================
    // 1. Interoperability Failure & Resilience Tests
    // =========================================================================





    // =========================================================================
    // 2. Consent Hardening & Default-Deny Tests
    // =========================================================================


    @Test
    void testConsent_Grant_ValidationAndAudit() {
        ConsentRequest req = new ConsentRequest();
        req.setApplicationId("APP-TEST");
        req.setServiceCode("SKILL_BENEFIT");
        req.setDataScope("skills");
        req.setPurpose("job_verification");
        req.setRequestingDepartmentId("DEPT-SKILL");

        com.mahasetu.securityworkflow.dto.ApplicationResponse mockApp = new com.mahasetu.securityworkflow.dto.ApplicationResponse();
        mockApp.setCitizenId("CIT-USER1");
        mockApp.setServiceCode("SKILL_BENEFIT");
        when(applicationServiceClient.getApplication("APP-TEST")).thenReturn(mockApp);

        com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy policy = new com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy("SKILL_BENEFIT", java.util.Set.of(com.mahasetu.securityworkflow.dto.DataScope.SKILLS), "job_verification", "skills", "DEPT-SKILLS", java.util.Set.of(), java.util.Set.of());
        when(consentPolicyService.getPolicy("SKILL_BENEFIT")).thenReturn(policy);

        when(consentRepository.save(any(Consent.class))).thenAnswer(inv -> {
            Consent consent = inv.getArgument(0);
            consent.setId(UUID.randomUUID());
            return consent;
        });

        Consent saved = consentService.grantConsent("CIT-USER1", req);

        assertNotNull(saved);
        assertEquals("GRANTED", saved.getStatus());
        verify(auditService).recordConsentGranted(eq("CIT-USER1"), eq(saved.getId()),
                eq("DEPT-SKILLS"), eq("SKILLS"), eq("job_verification"));
    }

    @Test
    void testConsent_Revoke_IdempotentOnSubsequentCalls() {
        UUID consentId = UUID.randomUUID();

        Consent consent = new Consent();
        consent.setId(consentId);
        consent.setCitizenId("CIT-USER1");
        consent.setStatus("GRANTED");

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        // First revoke
        consentService.revokeConsent("CIT-USER1", consentId);
        verify(auditService, times(1)).recordConsentRevoked("CIT-USER1", consentId);

        // Second revoke -> idempotent, no additional audit logging
        consent.setStatus("REVOKED");
        consentService.revokeConsent("CIT-USER1", consentId);
        verify(auditService, times(1)).recordConsentRevoked("CIT-USER1", consentId); // still 1
    }
}
