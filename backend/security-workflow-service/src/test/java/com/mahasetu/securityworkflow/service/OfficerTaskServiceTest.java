package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.OfficerDecisionResponse;
import com.mahasetu.securityworkflow.dto.OfficerReviewTaskResponse;
import com.mahasetu.securityworkflow.exception.InvalidTaskOperationException;
import com.mahasetu.securityworkflow.exception.TaskAlreadyCompletedException;
import com.mahasetu.securityworkflow.exception.TaskNotFoundException;
import com.mahasetu.securityworkflow.exception.ValidationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficerTaskServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private HistoryService historyService;

    @Mock
    private AuditService auditService;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private HistoricTaskInstanceQuery historicTaskQuery;

    private OfficerTaskService officerTaskService;

    @BeforeEach
    void setUp() {
        officerTaskService = new OfficerTaskService(taskService, historyService, auditService);
    }

    @Test
    void testGetPendingOfficerTasks_ReturnsMappedTasks() {
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("task-101");
        when(mockTask.getName()).thenReturn("Officer Review");
        when(mockTask.getProcessInstanceId()).thenReturn("proc-505");
        when(mockTask.getCreateTime()).thenReturn(new Date());
        when(mockTask.getAssignee()).thenReturn(null);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("UserTask_OfficerReview")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(taskQuery);
        when(taskQuery.desc()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(mockTask));

        when(taskService.getVariables("task-101")).thenReturn(Map.of(
                "applicationId", "APP-101",
                "citizenId", "CIT-101",
                "serviceCode", "SKILL_BENEFIT"
        ));

        List<OfficerReviewTaskResponse> tasks = officerTaskService.getPendingOfficerTasks();

        assertEquals(1, tasks.size());
        OfficerReviewTaskResponse response = tasks.get(0);
        assertEquals("task-101", response.getTaskId());
        assertEquals("APP-101", response.getApplicationId());
        assertEquals("CIT-101", response.getCitizenId());
        assertEquals("SKILL_BENEFIT", response.getServiceCode());
        assertEquals("PENDING_REVIEW", response.getStatus());
    }

    @Test
    void testCompleteOfficerDecision_Approve_CompletesAndAudits() {
        Task mockTask = mock(Task.class);
        when(mockTask.getProcessInstanceId()).thenReturn("proc-606");
        when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OfficerReview");
        when(mockTask.getAssignee()).thenReturn(null);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-202")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskService.getVariables("task-202")).thenReturn(Map.of(
                "applicationId", "APP-202"
        ));

        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(
                "task-202",
                "officer_patil",
                "APPROVE",
                "All criteria satisfied"
        );

        assertNotNull(response);
        assertEquals("task-202", response.getTaskId());
        assertEquals("APP-202", response.getApplicationId());
        assertEquals("APPROVE", response.getDecision());
        assertEquals("officer_patil", response.getOfficerId());
        assertEquals("COMPLETED", response.getStatus());

        ArgumentCaptor<Map<String, Object>> varCaptor = ArgumentCaptor.forClass(Map.class);
        verify(taskService).complete(eq("task-202"), varCaptor.capture());
        Map<String, Object> vars = varCaptor.getValue();
        assertEquals("APPROVE", vars.get("officerDecision"));
        assertEquals("officer_patil", vars.get("officerId"));
        assertEquals("All criteria satisfied", vars.get("officerDecisionReason"));

        verify(auditService).recordOfficerDecision(
                "APP-202",
                "proc-606",
                "task-202",
                "officer_patil",
                "APPROVE",
                "All criteria satisfied"
        );
    }

    @Test
    void testCompleteOfficerDecision_Reject_RequiresReason() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                officerTaskService.completeOfficerDecision("task-1", "officer_1", "REJECT", null));
        assertTrue(ex.getMessage().contains("Reason is required"));

        ValidationException exBlank = assertThrows(ValidationException.class, () ->
                officerTaskService.completeOfficerDecision("task-1", "officer_1", "REJECT", "   "));
        assertTrue(exBlank.getMessage().contains("Reason is required"));
    }

    @Test
    void testCompleteOfficerDecision_InvalidDecision_ThrowsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                officerTaskService.completeOfficerDecision("task-1", "officer_1", "MAYBE", "some reason"));
        assertTrue(ex.getMessage().contains("either APPROVE or REJECT"));
    }

    @Test
    void testCompleteOfficerDecision_TaskNotFound_ThrowsTaskNotFoundException() {
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-missing")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.taskId("task-missing")).thenReturn(historicTaskQuery);
        when(historicTaskQuery.singleResult()).thenReturn(null);

        assertThrows(TaskNotFoundException.class, () ->
                officerTaskService.completeOfficerDecision("task-missing", "officer_1", "APPROVE", null));
    }

    @Test
    void testCompleteOfficerDecision_AlreadyCompleted_ThrowsTaskAlreadyCompletedException() {
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-done")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);
        when(historicTask.getEndTime()).thenReturn(new Date());

        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.taskId("task-done")).thenReturn(historicTaskQuery);
        when(historicTaskQuery.singleResult()).thenReturn(historicTask);

        assertThrows(TaskAlreadyCompletedException.class, () ->
                officerTaskService.completeOfficerDecision("task-done", "officer_1", "APPROVE", null));
    }

    @Test
    void testClaimAndUnclaimTask_Success() {
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("task-303");
        when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OfficerReview");
        when(mockTask.getAssignee()).thenReturn(null).thenReturn("officer_deshmukh");

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-303")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskService.getVariables("task-303")).thenReturn(Map.of("applicationId", "APP-303"));

        OfficerReviewTaskResponse claimed = officerTaskService.claimTask("task-303", "officer_deshmukh");
        assertNotNull(claimed);
        verify(taskService).claim("task-303", "officer_deshmukh");

        // Now test unclaiming
        when(mockTask.getAssignee()).thenReturn("officer_deshmukh");
        officerTaskService.unclaimTask("task-303", "officer_deshmukh");
        verify(taskService).setAssignee("task-303", null);
    }

    @Test
    void testClaimTask_AlreadyClaimedByAnotherOfficer_ThrowsInvalidTaskOperationException() {
        Task mockTask = mock(Task.class);
        when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OfficerReview");
        when(mockTask.getAssignee()).thenReturn("officer_other");

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-303")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        assertThrows(InvalidTaskOperationException.class, () ->
                officerTaskService.claimTask("task-303", "officer_me"));
    }
}
