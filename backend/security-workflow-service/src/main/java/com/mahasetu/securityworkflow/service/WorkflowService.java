package com.mahasetu.securityworkflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final AuditService auditService;

    public WorkflowService(RuntimeService runtimeService, RepositoryService repositoryService, AuditService auditService) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
        this.auditService = auditService;
    }

    public String startWorkflow(String applicationId, String workflowKey) {
        log.info("[WORKFLOW_EVENT] Starting workflow: applicationId={}, workflowKey={}", applicationId, workflowKey);

        // Validate that the workflowKey exists as a deployed process definition
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(workflowKey)
                .latestVersion()
                .count();
        
        if (count == 0) {
            log.warn("[WORKFLOW_EVENT] Workflow start failed: unknown workflowKey={} for applicationId={}", workflowKey, applicationId);
            throw new IllegalArgumentException("Unknown or undeployed workflowKey: " + workflowKey);
        }

        // Idempotency: Check if an active process instance already exists for this applicationId and workflowKey
        ProcessInstance existing = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(workflowKey)
                .processInstanceBusinessKey(applicationId)
                .active()
                .singleResult();

        if (existing != null) {
            log.info("[WORKFLOW_EVENT] Workflow already active for applicationId={}, returning existing processInstanceId={}",
                    applicationId, existing.getId());
            return existing.getId();
        }

        // Start process instance and pass applicationId as a variable
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                workflowKey,
                applicationId, // Use applicationId as business key
                Map.of("applicationId", applicationId)
        );

        log.info("[WORKFLOW_EVENT] Workflow started successfully: applicationId={}, workflowKey={}, processInstanceId={}",
                applicationId, workflowKey, processInstance.getId());

        auditService.recordWorkflowStarted(applicationId, processInstance.getId(), workflowKey, "application-service");

        return processInstance.getId();
    }
}
