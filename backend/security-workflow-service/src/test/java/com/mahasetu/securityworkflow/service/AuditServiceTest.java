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
    void testGetAuditLogsForApplication_ReturnsRepositoryList() {
        AuditLog entry = new AuditLog("APP-1111", "officer_1", "OFFICER_REVIEW", "APPLICATION", "APP-1111", "APPROVE", "{}");
        when(auditLogRepository.findByApplicationId("APP-1111")).thenReturn(List.of(entry));

        List<AuditLog> logs = auditService.getAuditLogsForApplication("APP-1111");
        assertEquals(1, logs.size());
        assertEquals("APP-1111", logs.get(0).getApplicationId());
        verify(auditLogRepository).findByApplicationId("APP-1111");
    }
}
