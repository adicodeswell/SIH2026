package com.mahasetu.securityworkflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.entity.AuditLog;
import com.mahasetu.securityworkflow.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditService auditService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        auditService = new AuditService(auditLogRepository, objectMapper);
    }

    @Test
    void testRecordOfficerDecision_PersistsCorrectFields() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(UUID.randomUUID());
            return log;
        });

        AuditLog result = auditService.recordOfficerDecision(
                "APP-55555",
                "proc-inst-123",
                "task-999",
                "officer_sharma",
                "APPROVE",
                "All documents verified"
        );

        assertNotNull(result);
        assertNotNull(result.getId());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("APP-55555", saved.getApplicationId());
        assertEquals("officer_sharma", saved.getActorId());
        assertEquals("OFFICER_REVIEW", saved.getAction());
        assertEquals("APPLICATION", saved.getResourceType());
        assertEquals("APP-55555", saved.getResourceId());
        assertEquals("APPROVE", saved.getPurpose());
        assertNotNull(saved.getOccurredAt());
        assertTrue(saved.getMetadata().contains("task-999"));
        assertTrue(saved.getMetadata().contains("proc-inst-123"));
        assertTrue(saved.getMetadata().contains("All documents verified"));
    }

    @Test
    void testRecordConsentGranted_PersistsCorrectFields() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID consentId = UUID.randomUUID();
        AuditLog result = auditService.recordConsentGranted("CIT-123", consentId, "DEPT-1", "education", "verification", null);

        assertNotNull(result);
        assertEquals("CIT-123", result.getActorId());
        assertEquals("CONSENT_GRANTED", result.getAction());
        assertEquals("CONSENT", result.getResourceType());
        assertEquals(consentId.toString(), result.getResourceId());
        assertEquals("verification", result.getPurpose());
        assertTrue(result.getMetadata().contains("education"));
        assertTrue(result.getMetadata().contains("DEPT-1"));
    }

    @Test
    void testRecordConsentRevoked_PersistsCorrectFields() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID consentId = UUID.randomUUID();
        AuditLog result = auditService.recordConsentRevoked("CIT-123", consentId, null);

        assertNotNull(result);
        assertEquals("CIT-123", result.getActorId());
        assertEquals("CONSENT_REVOKED", result.getAction());
        assertEquals("CONSENT", result.getResourceType());
        assertEquals(consentId.toString(), result.getResourceId());
    }

    @Test
    void testRecordWorkflowStarted_PersistsCorrectFields() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog result = auditService.recordWorkflowStarted("APP-100", "proc-100", "application-orchestration", "application-service");

        assertNotNull(result);
        assertEquals("APP-100", result.getApplicationId());
        assertEquals("application-service", result.getActorId());
        assertEquals("WORKFLOW_STARTED", result.getAction());
        assertEquals("WORKFLOW", result.getResourceType());
        assertEquals("proc-100", result.getResourceId());
        assertTrue(result.getMetadata().contains("application-orchestration"));
    }

    @Test
    void testRecordWorkflowFailed_PersistsCorrectFields() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog result = auditService.recordWorkflowFailed("APP-100", "proc-100", "Downstream timeout", "security-workflow-service");

        assertNotNull(result);
        assertEquals("APP-100", result.getApplicationId());
        assertEquals("WORKFLOW_FAILED", result.getAction());
        assertTrue(result.getMetadata().contains("Downstream timeout"));
    }

    @Test
    void testRecordOfficerClaimAndUnclaim_PersistsCorrectFields() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog claim = auditService.recordOfficerClaim("APP-100", "task-1", "officer_1");
        assertEquals("OFFICER_CLAIM", claim.getAction());
        assertEquals("officer_1", claim.getActorId());

        AuditLog unclaim = auditService.recordOfficerUnclaim("APP-100", "task-1", "officer_1");
        assertEquals("OFFICER_UNCLAIM", unclaim.getAction());
        assertEquals("officer_1", unclaim.getActorId());
    }
}
