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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WorkflowServiceTest {

    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private AuditService auditService;
    private WorkflowService workflowService;

    @BeforeEach
    public void setup() {
        runtimeService = mock(RuntimeService.class);
        repositoryService = mock(RepositoryService.class);
        auditService = mock(AuditService.class);
        workflowService = new WorkflowService(runtimeService, repositoryService, auditService);
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

        org.camunda.bpm.engine.runtime.ProcessInstanceQuery piQuery = mock(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(piQuery);
        when(piQuery.processDefinitionKey(workflowKey)).thenReturn(piQuery);
        when(piQuery.processInstanceBusinessKey(applicationId)).thenReturn(piQuery);
        when(piQuery.active()).thenReturn(piQuery);
        when(piQuery.singleResult()).thenReturn(null);

        ProcessInstance processInstanceMock = mock(ProcessInstance.class);
        when(processInstanceMock.getId()).thenReturn(processInstanceId);

        when(runtimeService.startProcessInstanceByKey(
                eq(workflowKey),
                eq(applicationId),
                anyMap()
        )).thenReturn(processInstanceMock);

        String result = workflowService.startWorkflow(applicationId, workflowKey);

        assertEquals(processInstanceId, result);
        verify(auditService).recordWorkflowStarted(eq(applicationId), eq(processInstanceId), eq(workflowKey), eq("application-service"));
    }

    @Test
    public void testStartWorkflow_AlreadyActive_ReturnsExistingInstanceIdempotently() {
        String applicationId = "APP-123";
        String workflowKey = "common-review";
        String existingProcessInstanceId = "PI-EXISTING-999";

        ProcessDefinitionQuery queryMock = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(queryMock);
        when(queryMock.processDefinitionKey(workflowKey)).thenReturn(queryMock);
        when(queryMock.latestVersion()).thenReturn(queryMock);
        when(queryMock.count()).thenReturn(1L);

        ProcessInstance existingInstance = mock(ProcessInstance.class);
        when(existingInstance.getId()).thenReturn(existingProcessInstanceId);

        org.camunda.bpm.engine.runtime.ProcessInstanceQuery piQuery = mock(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(piQuery);
        when(piQuery.processDefinitionKey(workflowKey)).thenReturn(piQuery);
        when(piQuery.processInstanceBusinessKey(applicationId)).thenReturn(piQuery);
        when(piQuery.active()).thenReturn(piQuery);
        when(piQuery.singleResult()).thenReturn(existingInstance);

        String result = workflowService.startWorkflow(applicationId, workflowKey);

        assertEquals(existingProcessInstanceId, result);
        // Verify startProcessInstanceByKey was NOT called again
        verify(runtimeService, never()).startProcessInstanceByKey(any(), any(), anyMap());
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
