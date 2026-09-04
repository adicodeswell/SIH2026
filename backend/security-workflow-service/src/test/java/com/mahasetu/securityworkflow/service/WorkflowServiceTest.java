package com.mahasetu.securityworkflow.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkflowServiceTest {

    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private WorkflowService workflowService;

    @BeforeEach
    public void setup() {
        runtimeService = mock(RuntimeService.class);
        repositoryService = mock(RepositoryService.class);
        workflowService = new WorkflowService(runtimeService, repositoryService);
    }

    @Test
    public void testStartWorkflow_Success() {
        String applicationId = "APP-123";
        String workflowKey = "common-review";
        String processInstanceId = "PI-456";

        ProcessDefinitionQuery queryMock = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(queryMock);
        when(queryMock.processDefinitionKey(workflowKey)).thenReturn(queryMock);
        when(queryMock.latestVersion()).thenReturn(queryMock);
        when(queryMock.count()).thenReturn(1L);

        ProcessInstance processInstanceMock = mock(ProcessInstance.class);
        when(processInstanceMock.getId()).thenReturn(processInstanceId);

        when(runtimeService.startProcessInstanceByKey(
                eq(workflowKey),
                eq(applicationId),
                anyMap()
        )).thenReturn(processInstanceMock);

        String result = workflowService.startWorkflow(applicationId, workflowKey);

        assertEquals(processInstanceId, result);
    }

    @Test
    public void testStartWorkflow_UnknownKey() {
        String applicationId = "APP-123";
        String workflowKey = "unknown-workflow";

        ProcessDefinitionQuery queryMock = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(queryMock);
        when(queryMock.processDefinitionKey(workflowKey)).thenReturn(queryMock);
        when(queryMock.latestVersion()).thenReturn(queryMock);
        when(queryMock.count()).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.startWorkflow(applicationId, workflowKey);
        });
    }
}
