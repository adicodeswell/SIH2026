package com.mahasetu.securityworkflow.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorkflowService {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;

    public WorkflowService(RuntimeService runtimeService, RepositoryService repositoryService) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
    }

    public String startWorkflow(String applicationId, String workflowKey) {
        // Validate that the workflowKey exists as a deployed process definition
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(workflowKey)
                .latestVersion()
                .count();
        
        if (count == 0) {
            throw new IllegalArgumentException("Unknown or undeployed workflowKey: " + workflowKey);
        }

        // Start process instance and pass applicationId as a variable
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                workflowKey,
                applicationId, // Use applicationId as business key
                Map.of("applicationId", applicationId)
        );

        return processInstance.getId();
    }
}
