package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.OfficerDecisionResponse;
import com.mahasetu.securityworkflow.dto.OfficerReviewTaskResponse;
import com.mahasetu.securityworkflow.exception.InvalidTaskOperationException;
import com.mahasetu.securityworkflow.exception.TaskAlreadyCompletedException;
import com.mahasetu.securityworkflow.exception.TaskNotFoundException;
import com.mahasetu.securityworkflow.exception.ValidationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service encapsulating Camunda task management for human-in-the-loop officer reviews.
 * Handles task querying, claiming, completion, idempotency checks, and audit recording.
 */
@Service
public class OfficerTaskService {

    private static final Logger log = LoggerFactory.getLogger(OfficerTaskService.class);
    public static final String OFFICER_TASK_DEFINITION_KEY = "UserTask_OfficerReview";

    private final TaskService taskService;
    private final HistoryService historyService;
    private final AuditService auditService;

    public OfficerTaskService(TaskService taskService, HistoryService historyService, AuditService auditService) {
        this.taskService = taskService;
        this.historyService = historyService;
        this.auditService = auditService;
    }

    /**
     * Lists all pending, active officer review tasks in Camunda.
     */
    public List<OfficerReviewTaskResponse> getPendingOfficerTasks() {
        log.info("Querying active officer review tasks");
        List<Task> tasks = taskService.createTaskQuery()
                .taskDefinitionKey(OFFICER_TASK_DEFINITION_KEY)
                .active()
                .orderByTaskCreateTime()
                .desc()
                .list();

        List<OfficerReviewTaskResponse> responses = new ArrayList<>();
        for (Task task : tasks) {
            responses.add(mapToResponse(task));
        }
        log.info("Found {} pending officer review tasks", responses.size());
        return responses;
    }

    /**
     * Retrieves a specific officer review task by its Camunda task ID.
     */
    public OfficerReviewTaskResponse getOfficerTaskById(String taskId) {
        Task task = findActiveOfficerTaskOrThrow(taskId);
        return mapToResponse(task);
    }

    /**
     * Claims a task on behalf of the authenticated officer.
     */
    public OfficerReviewTaskResponse claimTask(String taskId, String officerId) {
        log.info("[OFFICER_EVENT] Officer {} attempting to claim task {}", officerId, taskId);
        Task task = findActiveOfficerTaskOrThrow(taskId);

        if (task.getAssignee() != null) {
            if (task.getAssignee().equals(officerId)) {
                log.info("[OFFICER_EVENT] Task {} is already claimed by officer {}", taskId, officerId);
                return mapToResponse(task);
            }
            throw new InvalidTaskOperationException("Task " + taskId + " is already claimed by another officer: " + task.getAssignee());
        }

        taskService.claim(taskId, officerId);
        log.info("[OFFICER_EVENT] Task {} successfully claimed by officer {}", taskId, officerId);
        Map<String, Object> variables = taskService.getVariables(taskId);
        String applicationId = variables != null ? (String) variables.get("applicationId") : null;
        auditService.recordOfficerClaim(applicationId, taskId, officerId);

        Task updatedTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        return mapToResponse(updatedTask != null ? updatedTask : task);
    }

    /**
     * Unclaims a task previously claimed by the authenticated officer.
     */
    public OfficerReviewTaskResponse unclaimTask(String taskId, String officerId) {
        log.info("[OFFICER_EVENT] Officer {} attempting to unclaim task {}", officerId, taskId);
        Task task = findActiveOfficerTaskOrThrow(taskId);

        if (task.getAssignee() == null) {
            return mapToResponse(task);
        }

        if (!task.getAssignee().equals(officerId)) {
            throw new InvalidTaskOperationException("Cannot unclaim task " + taskId + " claimed by another officer: " + task.getAssignee());
        }

        Map<String, Object> variables = taskService.getVariables(taskId);
        String applicationId = variables != null ? (String) variables.get("applicationId") : null;
        taskService.setAssignee(taskId, null);
        log.info("[OFFICER_EVENT] Task {} successfully unclaimed by officer {}", taskId, officerId);
        auditService.recordOfficerUnclaim(applicationId, taskId, officerId);

        Task updatedTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        return mapToResponse(updatedTask != null ? updatedTask : task);
    }

    /**
     * Submits an officer review decision (APPROVE or REJECT).
     * Validates task state, ensures idempotency, records audit log, and resumes the BPMN process.
     */
    @Transactional
    public OfficerDecisionResponse completeOfficerDecision(String taskId, String officerId, String decision, String reason) {
        if (decision == null) {
            throw new ValidationException("Decision must not be null");
        }

        String normalizedDecision = decision.trim().toUpperCase();
        if (!"APPROVE".equals(normalizedDecision) && !"REJECT".equals(normalizedDecision)) {
            throw new ValidationException("Decision must be either APPROVE or REJECT");
        }

        if ("REJECT".equals(normalizedDecision) && (reason == null || reason.trim().isEmpty())) {
            throw new ValidationException("Reason is required when rejecting an application");
        }

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .active()
                .singleResult();

        if (task == null) {
            HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .singleResult();

            if (historicTask != null && historicTask.getEndTime() != null) {
                log.warn("Task {} exists in history and has already completed", taskId);
                org.camunda.bpm.engine.history.HistoricVariableInstance histOfficer = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicTask.getProcessInstanceId())
                        .variableName("officerId")
                        .singleResult();
                org.camunda.bpm.engine.history.HistoricVariableInstance histDecision = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicTask.getProcessInstanceId())
                        .variableName("officerDecision")
                        .singleResult();
                org.camunda.bpm.engine.history.HistoricVariableInstance histAppId = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicTask.getProcessInstanceId())
                        .variableName("applicationId")
                        .singleResult();

                if (histOfficer != null && officerId.equals(histOfficer.getValue()) &&
                    histDecision != null && normalizedDecision.equals(histDecision.getValue())) {
                    log.info("Duplicate decision detected for completed task {}: returning idempotent response", taskId);
                    return new OfficerDecisionResponse(
                            taskId,
                            histAppId != null ? (String) histAppId.getValue() : null,
                            normalizedDecision,
                            officerId,
                            reason,
                            LocalDateTime.now(),
                            "COMPLETED"
                    );
                }
                throw new TaskAlreadyCompletedException(taskId);
            }

            log.warn("Task {} was not found", taskId);
            throw new TaskNotFoundException(taskId);
        }

        if (!OFFICER_TASK_DEFINITION_KEY.equals(task.getTaskDefinitionKey())) {
            log.warn("Task {} has definition key {} which is not an officer review task",
                    taskId, task.getTaskDefinitionKey());
            throw new InvalidTaskOperationException("Task " + taskId + " is not an officer review task");
        }

        if (task.getAssignee() != null && !task.getAssignee().equals(officerId)) {
            throw new InvalidTaskOperationException("Task " + taskId + " is claimed by another officer: " + task.getAssignee());
        }

        Map<String, Object> variables = taskService.getVariables(taskId);
        String applicationId = (String) variables.get("applicationId");
        String processInstanceId = task.getProcessInstanceId();

        log.info("Completing officer review task: taskId={}, applicationId={}, officerId={}, decision={}",
                taskId, applicationId, officerId, normalizedDecision);

        Map<String, Object> completionVariables = new HashMap<>();
        completionVariables.put("officerId", officerId);
        completionVariables.put("officerDecision", normalizedDecision);
        if (reason != null && !reason.trim().isEmpty()) {
            completionVariables.put("officerDecisionReason", reason);
            if ("REJECT".equals(normalizedDecision)) {
                completionVariables.put("failureReason", reason);
            }
        }
        completionVariables.put("officerDecisionTimestamp", LocalDateTime.now().toString());

        try {
            taskService.complete(taskId, completionVariables);
        } catch (OptimisticLockingException e) {
            log.warn("Concurrent modification on task {}: {}", taskId, e.getClass().getSimpleName());
            throw new TaskAlreadyCompletedException(taskId);
        } catch (ProcessEngineException e) {
            log.error("ProcessEngineException completing task {}: {}", taskId, e.getClass().getSimpleName());
            throw new TaskAlreadyCompletedException(taskId);
        }

        // Record immutable audit entry
        auditService.recordOfficerDecision(
                applicationId,
                processInstanceId,
                taskId,
                officerId,
                normalizedDecision,
                reason
        );

        log.info("Officer review task {} successfully completed with decision {}", taskId, normalizedDecision);

        return new OfficerDecisionResponse(
                taskId,
                applicationId,
                normalizedDecision,
                officerId,
                reason,
                LocalDateTime.now(),
                "COMPLETED"
        );
    }

    private Task findActiveOfficerTaskOrThrow(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .active()
                .singleResult();

        if (task == null) {
            HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .singleResult();

            if (historicTask != null && historicTask.getEndTime() != null) {
                log.warn("Task {} exists in history and has already completed", taskId);
                throw new TaskAlreadyCompletedException(taskId);
            }

            log.warn("Task {} was not found", taskId);
            throw new TaskNotFoundException(taskId);
        }

        if (!OFFICER_TASK_DEFINITION_KEY.equals(task.getTaskDefinitionKey())) {
            log.warn("Task {} has definition key {} which is not an officer review task",
                    taskId, task.getTaskDefinitionKey());
            throw new InvalidTaskOperationException("Task " + taskId + " is not an officer review task");
        }

        return task;
    }

    private OfficerReviewTaskResponse mapToResponse(Task task) {
        Map<String, Object> variables = taskService.getVariables(task.getId());
        String applicationId = (String) variables.get("applicationId");
        String citizenId = (String) variables.get("citizenId");
        String serviceCode = (String) variables.get("serviceCode");

        return new OfficerReviewTaskResponse(
                task.getId(),
                task.getName(),
                applicationId,
                task.getProcessInstanceId(),
                citizenId,
                serviceCode,
                task.getCreateTime(),
                "OFFICER",
                task.getAssignee(),
                "PENDING_REVIEW"
        );
    }
}
