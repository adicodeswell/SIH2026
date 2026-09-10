package com.mahasetu.securityworkflow.controller;

import com.mahasetu.securityworkflow.entity.AuditLog;
import com.mahasetu.securityworkflow.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/audit/applications")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasRole('SERVICE') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getApplicationAuditLogs(@PathVariable String applicationId) {
        return ResponseEntity.ok(auditLogRepository.findByApplicationIdOrderByOccurredAtAsc(applicationId));
    }
}
