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
    private com.mahasetu.securityworkflow.service.ConsentPolicyService consentPolicyService;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private HistoricTaskInstanceQuery historicTaskQuery;

    @Mock
    

    private OfficerTaskService officerTaskService;

    @BeforeEach
    void setUp() {
        com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy policy = new com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy(
            "SKILL_BENEFIT", java.util.Collections.emptySet(), "", "", "SKILLS", java.util.Collections.emptySet(), java.util.Collections.emptySet()
        );
        lenient().when(consentPolicyService.getPolicy(any())).thenReturn(policy);

        officerTaskService = new OfficerTaskService(taskService, historyService, auditService, consentPolicyService);
    }

    @Test
    void testGetPendingOfficerTasks_ReturnsMappedTasks() {
        Task mockTask = mock(Task.class);
        lenient().when(mockTask.getId()).thenReturn("task-101");
        lenient().when(mockTask.getName()).thenReturn("Officer Review");
        lenient().when(mockTask.getProcessInstanceId()).thenReturn("proc-505");
        lenient().when(mockTask.getCreateTime()).thenReturn(new Date());
        lenient().when(mockTask.getAssignee()).thenReturn("officer_patil");

        lenient().when(taskService.createTaskQuery()).thenReturn(taskQuery);
        lenient().when(taskQuery.taskDefinitionKey("UserTask_OfficerReview")).thenReturn(taskQuery);
        lenient().when(taskQuery.active()).thenReturn(taskQuery);
        lenient().when(taskQuery.orderByTaskCreateTime()).thenReturn(taskQuery);
        lenient().when(taskQuery.desc()).thenReturn(taskQuery);
        lenient().when(taskQuery.list()).thenReturn(List.of(mockTask));

        lenient().when(taskService.getVariables("task-101")).thenReturn(Map.of(
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
        lenient().when(mockTask.getProcessInstanceId()).thenReturn("proc-606");
        lenient().when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OfficerReview");
        lenient().when(mockTask.getAssignee()).thenReturn("officer_patil");

        lenient().when(taskService.createTaskQuery()).thenReturn(taskQuery);
        lenient().when(taskQuery.taskId("task-202")).thenReturn(taskQuery);
        lenient().when(taskQuery.active()).thenReturn(taskQuery);
        lenient().when(taskQuery.singleResult()).thenReturn(mockTask);

        lenient().when(taskService.getVariables("task-202")).thenReturn(Map.of("applicationId", "APP-202", "serviceCode", "SKILL_BENEFIT"));

        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision("task-202", "officer_patil", "SKILLS", "APPROVE", "All criteria satisfied"
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
                officerTaskService.completeOfficerDecision("task-1", "officer_1", "SKILLS", "REJECT", null));
        assertTrue(ex.getMessage().contains("Reason is required"));

        ValidationException exBlank = assertThrows(ValidationException.class, () ->
                officerTaskService.completeOfficerDecision("task-1", "officer_1", "SKILLS", "REJECT", "   "));
        assertTrue(exBlank.getMessage().contains("Reason is required"));
    }

    @Test
    void testCompleteOfficerDecision_InvalidDecision_ThrowsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                officerTaskService.completeOfficerDecision("task-1", "officer_1", "SKILLS", "MAYBE", "some reason"));
        assertTrue(ex.getMessage().contains("either APPROVE or REJECT"));
    }

    @Test
    void testCompleteOfficerDecision_TaskNotFound_ThrowsTaskNotFoundException() {
        lenient().when(taskService.createTaskQuery()).thenReturn(taskQuery);
        lenient().when(taskQuery.taskId("task-missing")).thenReturn(taskQuery);
        lenient().when(taskQuery.active()).thenReturn(taskQuery);
        lenient().when(taskQuery.singleResult()).thenReturn(null);

        lenient().when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.taskId("task-missing")).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.singleResult()).thenReturn(null);

        assertThrows(TaskNotFoundException.class, () ->
                officerTaskService.completeOfficerDecision("task-missing", "officer_1", "SKILLS", "APPROVE", null));
    }

    @Test
    void testCompleteOfficerDecision_AlreadyCompleted_DifferentDecision_ThrowsTaskAlreadyCompletedException() {
        
        
        org.camunda.bpm.engine.task.TaskQuery mockTaskQuery = mock(org.camunda.bpm.engine.task.TaskQuery.class);
        lenient().when(taskService.createTaskQuery()).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.taskId(anyString())).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.active()).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.singleResult()).thenReturn(null);
    
        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);
        lenient().when(historicTask.getEndTime()).thenReturn(new java.util.Date());
        lenient().when(historicTask.getProcessInstanceId()).thenReturn("proc-done");

        lenient().when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.taskId(anyString())).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.singleResult()).thenReturn(historicTask);

        org.camunda.bpm.engine.history.HistoricVariableInstanceQuery smartVarQuery = mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
        lenient().when(historyService.createHistoricVariableInstanceQuery()).thenReturn(smartVarQuery);
        lenient().when(smartVarQuery.processInstanceId(anyString())).thenReturn(smartVarQuery);
        lenient().when(smartVarQuery.variableName(anyString())).thenAnswer(inv -> {
            String varName = inv.getArgument(0);
            org.camunda.bpm.engine.history.HistoricVariableInstanceQuery mockQuery = mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
            org.camunda.bpm.engine.history.HistoricVariableInstance mockVar = mock(org.camunda.bpm.engine.history.HistoricVariableInstance.class);
            
            if ("applicationId".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("APP-1001");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("serviceCode".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("SKILLS");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerId".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("officer_1");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerDecision".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("APPROVE");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else {
                lenient().when(mockQuery.singleResult()).thenReturn(null);
            }
            return mockQuery;
        });

        com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy policy = new com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy(
            "SKILLS", java.util.Set.of(), "verification", "NONE", "SKILLS", java.util.Set.of(), java.util.Set.of()
        );
        lenient().when(consentPolicyService.getPolicy("SKILLS")).thenReturn(policy);

        
        assertThrows(TaskAlreadyCompletedException.class, () ->
                officerTaskService.completeOfficerDecision("task-done", "officer_1", "SKILLS", "REJECT", "Reason"));
    
    }

    @Test
    void testCompleteOfficerDecision_DuplicateSameDecision_ReturnsIdempotentResponse() {
        
        
        org.camunda.bpm.engine.task.TaskQuery mockTaskQuery = mock(org.camunda.bpm.engine.task.TaskQuery.class);
        lenient().when(taskService.createTaskQuery()).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.taskId(anyString())).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.active()).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.singleResult()).thenReturn(null);
    
        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);
        lenient().when(historicTask.getEndTime()).thenReturn(new java.util.Date());
        lenient().when(historicTask.getProcessInstanceId()).thenReturn("proc-done");

        lenient().when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.taskId(anyString())).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.singleResult()).thenReturn(historicTask);

        org.camunda.bpm.engine.history.HistoricVariableInstanceQuery smartVarQuery = mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
        lenient().when(historyService.createHistoricVariableInstanceQuery()).thenReturn(smartVarQuery);
        lenient().when(smartVarQuery.processInstanceId(anyString())).thenReturn(smartVarQuery);
        lenient().when(smartVarQuery.variableName(anyString())).thenAnswer(inv -> {
            String varName = inv.getArgument(0);
            org.camunda.bpm.engine.history.HistoricVariableInstanceQuery mockQuery = mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
            org.camunda.bpm.engine.history.HistoricVariableInstance mockVar = mock(org.camunda.bpm.engine.history.HistoricVariableInstance.class);
            
            if ("applicationId".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("APP-1001");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("serviceCode".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("SKILLS");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerId".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("officer_1");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerDecision".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("APPROVE");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else {
                lenient().when(mockQuery.singleResult()).thenReturn(null);
            }
            return mockQuery;
        });

        com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy policy = new com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy(
            "SKILLS", java.util.Set.of(), "verification", "NONE", "SKILLS", java.util.Set.of(), java.util.Set.of()
        );
        lenient().when(consentPolicyService.getPolicy("SKILLS")).thenReturn(policy);

        
        com.mahasetu.securityworkflow.dto.OfficerDecisionResponse response = officerTaskService.completeOfficerDecision("task-done", "officer_1", "SKILLS", "APPROVE", null);
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("APPROVE", response.getDecision());
    
    }

    @Test
    void testClaimAndUnclaimTask_Success() {
        Task mockTask = mock(Task.class);
        lenient().when(mockTask.getId()).thenReturn("task-303");
        lenient().when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OfficerReview");
        lenient().when(mockTask.getAssignee()).thenReturn(null).thenReturn("officer_deshmukh");

        lenient().when(taskService.createTaskQuery()).thenReturn(taskQuery);
        lenient().when(taskQuery.taskId("task-303")).thenReturn(taskQuery);
        lenient().when(taskQuery.active()).thenReturn(taskQuery);
        lenient().when(taskQuery.singleResult()).thenReturn(mockTask);

        lenient().when(taskService.getVariables("task-303")).thenReturn(Map.of("applicationId", "APP-303", "serviceCode", "SKILL_BENEFIT"));

        OfficerReviewTaskResponse claimed = officerTaskService.claimTask("task-303", "officer_deshmukh", "SKILLS");
        assertNotNull(claimed);
        verify(taskService).claim("task-303", "officer_deshmukh");
        verify(auditService).recordOfficerClaim("APP-303", "task-303", "officer_deshmukh");

        // Now test unclaiming
        lenient().when(mockTask.getAssignee()).thenReturn("officer_deshmukh");
        officerTaskService.unclaimTask("task-303", "officer_deshmukh", "SKILLS");
        verify(taskService).setAssignee("task-303", null);
        verify(auditService).recordOfficerUnclaim("APP-303", "task-303", "officer_deshmukh");
    }

    @Test
    void testClaimTask_AlreadyClaimedByAnotherOfficer_ThrowsInvalidTaskOperationException() {
        Task mockTask = mock(Task.class);
        lenient().when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OfficerReview");
        lenient().when(mockTask.getAssignee()).thenReturn("officer_other");

        lenient().when(taskService.createTaskQuery()).thenReturn(taskQuery);
        lenient().when(taskQuery.taskId("task-303")).thenReturn(taskQuery);
        lenient().when(taskQuery.active()).thenReturn(taskQuery);
        lenient().when(taskQuery.singleResult()).thenReturn(mockTask);

        assertThrows(InvalidTaskOperationException.class, () ->
                officerTaskService.claimTask("task-303", "officer_me", "SKILLS"));
    }
}
