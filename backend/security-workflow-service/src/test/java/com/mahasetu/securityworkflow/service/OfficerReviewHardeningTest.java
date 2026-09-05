package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.OfficerDecisionResponse;
import com.mahasetu.securityworkflow.dto.OfficerReviewTaskResponse;
import com.mahasetu.securityworkflow.exception.InvalidTaskOperationException;
import com.mahasetu.securityworkflow.exception.TaskAlreadyCompletedException;
import com.mahasetu.securityworkflow.exception.ValidationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OfficerReviewHardeningTest {

    @Mock
    private TaskService taskService;

    @Mock
    private HistoryService historyService;

    @Mock
    private AuditService auditService;

    @Mock
    private Task task;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private HistoricTaskInstance historicTaskInstance;

    @Mock
    private HistoricVariableInstanceQuery histVarQuery;

    @Mock
    private HistoricVariableInstance histOfficerVar;

    @Mock
    private HistoricVariableInstance histDecisionVar;

    @Mock
    private HistoricVariableInstance histAppIdVar;

    private OfficerTaskService officerTaskService;

    private static final String TASK_ID = "task-100";
    private static final String OFFICER_1 = "officer_1";
    private static final String OFFICER_2 = "officer_2";

    @BeforeEach
    void setUp() {
        officerTaskService = new OfficerTaskService(taskService, historyService, auditService);
    }

    private void setupActiveTaskMock(String assignee) {
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(TASK_ID)).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);

        when(task.getId()).thenReturn(TASK_ID);
        when(task.getTaskDefinitionKey()).thenReturn(OfficerTaskService.OFFICER_TASK_DEFINITION_KEY);
        when(task.getAssignee()).thenReturn(assignee);
        when(task.getCreateTime()).thenReturn(new Date());
        when(taskService.getVariables(TASK_ID)).thenReturn(Map.of("applicationId", "APP-100"));
    }

    @Test
    void testClaim_UnassignedTask_SucceedsAndAudits() {
        setupActiveTaskMock(null);

        OfficerReviewTaskResponse response = officerTaskService.claimTask(TASK_ID, OFFICER_1);

        assertNotNull(response);
        verify(taskService).claim(TASK_ID, OFFICER_1);
        verify(auditService).recordOfficerClaim("APP-100", TASK_ID, OFFICER_1);
    }

    @Test
    void testClaim_AlreadyClaimedBySameOfficer_IdempotentSuccess() {
        setupActiveTaskMock(OFFICER_1);

        OfficerReviewTaskResponse response = officerTaskService.claimTask(TASK_ID, OFFICER_1);

        assertNotNull(response);
        verify(taskService, never()).claim(anyString(), anyString());
    }

    @Test
    void testClaim_AlreadyClaimedByAnotherOfficer_ThrowsInvalidTaskOperation() {
        setupActiveTaskMock(OFFICER_2);

        assertThrows(InvalidTaskOperationException.class, () -> officerTaskService.claimTask(TASK_ID, OFFICER_1));
    }

    @Test
    void testUnclaim_AssignedOfficer_SucceedsAndAudits() {
        setupActiveTaskMock(OFFICER_1);

        OfficerReviewTaskResponse response = officerTaskService.unclaimTask(TASK_ID, OFFICER_1);

        assertNotNull(response);
        verify(taskService).setAssignee(TASK_ID, null);
        verify(auditService).recordOfficerUnclaim("APP-100", TASK_ID, OFFICER_1);
    }

    @Test
    void testUnclaim_WrongOfficer_ThrowsInvalidTaskOperation() {
        setupActiveTaskMock(OFFICER_2);

        assertThrows(InvalidTaskOperationException.class, () -> officerTaskService.unclaimTask(TASK_ID, OFFICER_1));
    }

    @Test
    void testUnclaim_UnassignedTask_IsSafe() {
        setupActiveTaskMock(null);

        OfficerReviewTaskResponse response = officerTaskService.unclaimTask(TASK_ID, OFFICER_1);

        assertNotNull(response);
        verify(taskService, never()).setAssignee(anyString(), any());
    }

    @Test
    void testCompleteDecision_Approve_SucceedsAndAudits() {
        setupActiveTaskMock(OFFICER_1);

        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, "APPROVE", "Looks good");

        assertNotNull(response);
        assertEquals("APPROVE", response.getDecision());
        assertEquals("COMPLETED", response.getStatus());

        verify(taskService).complete(eq(TASK_ID), anyMap());
        verify(auditService).recordOfficerDecision(eq("APP-100"), any(), eq(TASK_ID), eq(OFFICER_1), eq("APPROVE"), eq("Looks good"));
    }

    @Test
    void testCompleteDecision_RejectWithReason_SucceedsAndAudits() {
        setupActiveTaskMock(OFFICER_1);

        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, "REJECT", "Document missing");

        assertNotNull(response);
        assertEquals("REJECT", response.getDecision());
        verify(auditService).recordOfficerDecision(eq("APP-100"), any(), eq(TASK_ID), eq(OFFICER_1), eq("REJECT"), eq("Document missing"));
    }

    @Test
    void testCompleteDecision_RejectWithoutReason_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, "REJECT", null));
        assertThrows(ValidationException.class, () -> officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, "REJECT", "   "));
    }

    @Test
    void testCompleteDecision_NullOrInvalidDecision_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, null, "Reason"));
        assertThrows(ValidationException.class, () -> officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, "MAYBE", "Reason"));
    }

    @Test
    void testCompleteDecision_CompletedTask_SameOfficerSameDecision_IdempotentResponse() {
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(TASK_ID)).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null); // Not active

        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(mock(org.camunda.bpm.engine.history.HistoricTaskInstanceQuery.class));
        org.camunda.bpm.engine.history.HistoricTaskInstanceQuery histTaskQuery = historyService.createHistoricTaskInstanceQuery();
        when(histTaskQuery.taskId(TASK_ID)).thenReturn(histTaskQuery);
        when(histTaskQuery.singleResult()).thenReturn(historicTaskInstance);
        when(historicTaskInstance.getEndTime()).thenReturn(new Date());
        when(historicTaskInstance.getProcessInstanceId()).thenReturn("proc-100");

        when(historyService.createHistoricVariableInstanceQuery()).thenReturn(histVarQuery);
        when(histVarQuery.processInstanceId("proc-100")).thenReturn(histVarQuery);
        when(histVarQuery.variableName("officerId")).thenReturn(histVarQuery);
        when(histVarQuery.singleResult()).thenReturn(histOfficerVar);
        when(histOfficerVar.getValue()).thenReturn(OFFICER_1);

        HistoricVariableInstanceQuery histVarQuery2 = mock(HistoricVariableInstanceQuery.class);
        HistoricVariableInstanceQuery histVarQuery3 = mock(HistoricVariableInstanceQuery.class);

        when(historyService.createHistoricVariableInstanceQuery()).thenReturn(histVarQuery, histVarQuery2, histVarQuery3);
        when(histVarQuery2.processInstanceId("proc-100")).thenReturn(histVarQuery2);
        when(histVarQuery2.variableName("officerDecision")).thenReturn(histVarQuery2);
        when(histVarQuery2.singleResult()).thenReturn(histDecisionVar);
        when(histDecisionVar.getValue()).thenReturn("APPROVE");

        when(histVarQuery3.processInstanceId("proc-100")).thenReturn(histVarQuery3);
        when(histVarQuery3.variableName("applicationId")).thenReturn(histVarQuery3);
        when(histVarQuery3.singleResult()).thenReturn(histAppIdVar);
        when(histAppIdVar.getValue()).thenReturn("APP-100");

        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, "APPROVE", "Looks good");

        assertNotNull(response);
        assertEquals("APPROVE", response.getDecision());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    void testCompleteDecision_CompletedTask_DifferentOfficerOrConflictingDecision_ThrowsTaskAlreadyCompleted() {
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(TASK_ID)).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        org.camunda.bpm.engine.history.HistoricTaskInstanceQuery histTaskQuery = mock(org.camunda.bpm.engine.history.HistoricTaskInstanceQuery.class);
        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(histTaskQuery);
        when(histTaskQuery.taskId(TASK_ID)).thenReturn(histTaskQuery);
        when(histTaskQuery.singleResult()).thenReturn(historicTaskInstance);
        when(historicTaskInstance.getEndTime()).thenReturn(new Date());
        when(historicTaskInstance.getProcessInstanceId()).thenReturn("proc-100");

        when(historyService.createHistoricVariableInstanceQuery()).thenReturn(histVarQuery);
        when(histVarQuery.processInstanceId(anyString())).thenReturn(histVarQuery);
        when(histVarQuery.variableName(anyString())).thenReturn(histVarQuery);
        when(histVarQuery.singleResult()).thenReturn(histOfficerVar);
        when(histOfficerVar.getValue()).thenReturn(OFFICER_1);

        assertThrows(TaskAlreadyCompletedException.class, () -> officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_2, "APPROVE", "Looks good"));
    }
}
