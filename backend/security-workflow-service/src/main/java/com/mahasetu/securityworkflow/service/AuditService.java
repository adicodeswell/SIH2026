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
import java.util.UUID;

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

    @Transactional
    public AuditLog recordConsentGranted(String citizenId, UUID consentId, String departmentId, String dataScope, String purpose) {
        log.info("Recording consent granted audit: citizenId={}, consentId={}, departmentId={}",
                citizenId, consentId, departmentId);
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("departmentId", departmentId);
        metadataMap.put("dataScope", dataScope);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLog auditLog = new AuditLog(
                null,
                citizenId,
                "CONSENT_GRANTED",
                "CONSENT",
                consentId.toString(),
                purpose,
                metadataJson
        );
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog recordConsentRevoked(String citizenId, UUID consentId) {
        log.info("Recording consent revoked audit: citizenId={}, consentId={}", citizenId, consentId);
        AuditLog auditLog = new AuditLog(
                null,
                citizenId,
                "CONSENT_REVOKED",
                "CONSENT",
                consentId.toString(),
                "REVOCATION",
                "{}"
        );
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog recordWorkflowStarted(String applicationId, String processInstanceId, String workflowKey, String actorId) {
        log.info("Recording workflow started audit: applicationId={}, processInstanceId={}, workflowKey={}",
                applicationId, processInstanceId, workflowKey);
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("processInstanceId", processInstanceId);
        metadataMap.put("workflowKey", workflowKey);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLog auditLog = new AuditLog(
                applicationId,
                actorId != null ? actorId : "system",
                "WORKFLOW_STARTED",
                "WORKFLOW",
                processInstanceId,
                "START_WORKFLOW",
                metadataJson
        );
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog recordWorkflowFailed(String applicationId, String processInstanceId, String reason, String actorId) {
        log.info("Recording workflow failed audit: applicationId={}, processInstanceId={}, reason={}",
                applicationId, processInstanceId, reason);
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("processInstanceId", processInstanceId);
        metadataMap.put("reason", reason);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLog auditLog = new AuditLog(
                applicationId,
                actorId != null ? actorId : "security-workflow-service",
                "WORKFLOW_FAILED",
                "WORKFLOW",
                processInstanceId != null ? processInstanceId : applicationId,
                "EXECUTION_FAILURE",
                metadataJson
        );
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog recordOfficerClaim(String applicationId, String taskId, String officerId) {
        log.info("Recording officer claim audit: applicationId={}, taskId={}, officerId={}",
                applicationId, taskId, officerId);
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("taskId", taskId);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLog auditLog = new AuditLog(
                applicationId,
                officerId,
                "OFFICER_CLAIM",
                "TASK",
                taskId,
                "CLAIM",
                metadataJson
        );
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog recordOfficerUnclaim(String applicationId, String taskId, String officerId) {
        log.info("Recording officer unclaim audit: applicationId={}, taskId={}, officerId={}",
                applicationId, taskId, officerId);
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("taskId", taskId);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadataJson = "{}";
        }

        AuditLog auditLog = new AuditLog(
                applicationId,
                officerId,
                "OFFICER_UNCLAIM",
                "TASK",
                taskId,
                "UNCLAIM",
                metadataJson
        );
        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogsForApplication(String applicationId) {
        return auditLogRepository.findByApplicationId(applicationId);
    }
}
