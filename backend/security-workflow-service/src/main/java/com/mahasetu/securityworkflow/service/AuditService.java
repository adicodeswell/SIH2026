package com.mahasetu.securityworkflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.entity.AuditLog;
import com.mahasetu.securityworkflow.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for recording immutable audit logs of sensitive workflow decisions.
 * Identity is guaranteed to come from the authenticated context.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Records an officer's review decision immutably in the audit trail.
     *
     * @param applicationId     application being reviewed
     * @param processInstanceId Camunda process instance ID
     * @param taskId            Camunda task ID
     * @param officerId         authenticated officer identity (from JWT sub)
     * @param decision          APPROVE or REJECT
     * @param reason            reason provided by officer (optional for approve, required for reject)
     * @return persisted AuditLog
     */
    @Transactional
    public AuditLog recordOfficerDecision(String applicationId, String processInstanceId,
                                          String taskId, String officerId,
                                          String decision, String reason) {
        log.info("Recording audit log: applicationId={}, officerId={}, decision={}, taskId={}",
                applicationId, officerId, decision, taskId);

        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("processInstanceId", processInstanceId);
        metadataMap.put("taskId", taskId);
        if (reason != null && !reason.trim().isEmpty()) {
            metadataMap.put("reason", reason);
        }

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{\"taskId\":\"" + taskId + "\",\"processInstanceId\":\"" + processInstanceId + "\"}";
        }

        AuditLog auditLog = new AuditLog(
                applicationId,
                officerId,
                "OFFICER_REVIEW",
                "APPLICATION",
                applicationId,
                decision,
                metadataJson
        );
        auditLog.setOccurredAt(LocalDateTime.now());

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Audit log successfully persisted with id={}", saved.getId());
        return saved;
    }

    public List<AuditLog> getAuditLogsForApplication(String applicationId) {
        return auditLogRepository.findByApplicationId(applicationId);
    }
}
