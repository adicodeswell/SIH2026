package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.*;
import com.mahasetu.securityworkflow.exception.*;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfficerReviewAdversarialTest {

    @Mock private TaskService taskService;
    @Mock private HistoryService historyService;
    @Mock private AuditService auditService;
    @Mock private ConsentPolicyService consentPolicyService;

    @Mock private TaskQuery taskQuery;
    @Mock private Task mockTask;
    @Mock private HistoricTaskInstanceQuery historicTaskQuery;
    @Mock private HistoricVariableInstanceQuery historicVariableQuery;

    @InjectMocks
    private OfficerTaskService officerTaskService;

    private ResolvedConsentPolicy skillsPolicy;

    @BeforeEach
    void setUp() {
        skillsPolicy = new ResolvedConsentPolicy("SKILL_BENEFIT", java.util.Set.of(com.mahasetu.securityworkflow.dto.DataScope.EDUCATION, com.mahasetu.securityworkflow.dto.DataScope.SKILLS), "verification", "EDUCATION, SKILLS", "DEPT-SKILLS", java.util.Collections.emptySet(), java.util.Collections.emptySet());
        lenient().when(consentPolicyService.getPolicy("SKILL_BENEFIT")).thenReturn(skillsPolicy);

        lenient().when(taskService.createTaskQuery()).thenReturn(taskQuery);
        lenient().when(taskQuery.taskId(anyString())).thenReturn(taskQuery);
        lenient().when(taskQuery.active()).thenReturn(taskQuery);
        lenient().when(taskQuery.singleResult()).thenReturn(mockTask);
        
        lenient().when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.taskId(anyString())).thenReturn(historicTaskQuery);
        lenient().when(historicTaskQuery.singleResult()).thenReturn(null);
    }

    private void setupActiveTask(String assignee) {
        lenient().when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OfficerReview");
        lenient().when(mockTask.getAssignee()).thenReturn(assignee);
        lenient().when(mockTask.getProcessInstanceId()).thenReturn("proc-123");
        lenient().when(taskService.getVariables(anyString())).thenReturn(Map.of(
            "applicationId", "APP-1001",
            "serviceCode", "SKILL_BENEFIT"
        ));
    }

    // 7. Department authorization tests
    @Test
    void testClaim_CorrectDepartment_Allowed() {
        setupActiveTask(null);
        OfficerReviewTaskResponse resp = officerTaskService.claimTask("task-123", "officer1", "DEPT-SKILLS");
        assertNotNull(resp);
        verify(taskService).claim("task-123", "officer1");
    }

    @Test
    void testClaim_WrongDepartment_Denied() {
        setupActiveTask(null);
        assertThrows(AccessDeniedException.class, () -> 
            officerTaskService.claimTask("task-123", "officer1", "DEPT-EDU")
        );
        verify(taskService, never()).claim(anyString(), anyString());
    }
    
    @Test
    void testDecision_WrongDepartment_Denied() {
        setupActiveTask("officer1");
        assertThrows(AccessDeniedException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-EDU", "APPROVE", null)
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    @Test
    void testClaim_Normalization_Allowed() {
        setupActiveTask(null);
        // lowercase and trailing spaces in JWT department should be normalized and match
        OfficerReviewTaskResponse resp = officerTaskService.claimTask("task-123", "officer1", "  dept-skills  ");
        assertNotNull(resp);
        verify(taskService).claim("task-123", "officer1");
    }

    // 8. Unclaim authorization tests
    @Test
    void testUnclaim_UnclaimedTask_SafeNoOp() {
        setupActiveTask(null);
        OfficerReviewTaskResponse resp = officerTaskService.unclaimTask("task-123", "officer1", "DEPT-SKILLS");
        assertNotNull(resp);
        verify(taskService, never()).setAssignee(anyString(), any());
    }

    @Test
    void testUnclaim_ClaimedBySameOfficer_Allowed() {
        setupActiveTask("officer1");
        officerTaskService.unclaimTask("task-123", "officer1", "DEPT-SKILLS");
        verify(taskService).setAssignee("task-123", null);
    }

    @Test
    void testUnclaim_ClaimedByAnotherOfficer_Denied() {
        setupActiveTask("officer2");
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.unclaimTask("task-123", "officer1", "DEPT-SKILLS")
        );
        verify(taskService, never()).setAssignee(anyString(), any());
    }

    // 9. Decision authorization tests
    @Test
    void testDecision_UnclaimedApprove_Denied() {
        setupActiveTask(null);
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    @Test
    void testDecision_UnclaimedReject_Denied() {
        setupActiveTask(null);
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "REJECT", "Reason")
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    @Test
    void testDecision_ClaimedByAnother_Denied() {
        setupActiveTask("officer2");
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    @Test
    void testDecision_ClaimedBySame_Allowed() {
        setupActiveTask("officer1");
        officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null);
        verify(taskService).complete(eq("task-123"), any());
    }

    @Test
    void testDecision_RejectWithoutReason_Denied() {
        setupActiveTask("officer1");
        assertThrows(ValidationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "REJECT", null)
        );
        assertThrows(ValidationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "REJECT", "")
        );
        assertThrows(ValidationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "REJECT", "   ")
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    @Test
    void testDecision_InvalidDecision_Denied() {
        setupActiveTask("officer1");
        assertThrows(ValidationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "MAYBE", null)
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    // 10. Task Definition validation
    @Test
    void testDecision_WrongTaskDefinition_Denied() {
        setupActiveTask("officer1");
        lenient().when(mockTask.getTaskDefinitionKey()).thenReturn("UserTask_OtherProcess");
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    // 5. Idempotent Retry Authorization
    @Test
    void testDecision_CompletedTaskSameOfficer_AllowedIdempotent() {
        lenient().when(taskQuery.singleResult()).thenReturn(null);

        HistoricTaskInstance histTask = mock(HistoricTaskInstance.class);
        lenient().when(histTask.getEndTime()).thenReturn(new java.util.Date());
        lenient().when(histTask.getProcessInstanceId()).thenReturn("proc-123");
        lenient().when(historicTaskQuery.singleResult()).thenReturn(histTask);

        HistoricVariableInstance histOfficer = mock(HistoricVariableInstance.class);
        lenient().when(histOfficer.getValue()).thenReturn("officer1");
        HistoricVariableInstance histDecision = mock(HistoricVariableInstance.class);
        lenient().when(histDecision.getValue()).thenReturn("APPROVE");
        
        HistoricVariableInstanceQuery q1 = mock(HistoricVariableInstanceQuery.class);
        HistoricVariableInstanceQuery q2 = mock(HistoricVariableInstanceQuery.class);
        HistoricVariableInstanceQuery q3 = mock(HistoricVariableInstanceQuery.class);

        lenient().when(historyService.createHistoricVariableInstanceQuery()).thenReturn(q1, q2, q3);
        
        lenient().when(q1.processInstanceId("proc-123")).thenReturn(q1);
        lenient().when(q1.variableName("officerId")).thenReturn(q1);
        lenient().when(q1.singleResult()).thenReturn(histOfficer);

        lenient().when(q2.processInstanceId("proc-123")).thenReturn(q2);
        lenient().when(q2.variableName("officerDecision")).thenReturn(q2);
        lenient().when(q2.singleResult()).thenReturn(histDecision);

        lenient().when(q3.processInstanceId("proc-123")).thenReturn(q3);
        lenient().when(q3.variableName("applicationId")).thenReturn(q3);
        lenient().when(q3.singleResult()).thenReturn(null);

        OfficerDecisionResponse resp = officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null);
        assertEquals("COMPLETED", resp.getStatus());
        assertEquals("APPROVE", resp.getDecision());
    }

    @Test
    void testDecision_CompletedTaskDifferentOfficer_Throws() {
        lenient().when(taskQuery.singleResult()).thenReturn(null);

        HistoricTaskInstance histTask = mock(HistoricTaskInstance.class);
        lenient().when(histTask.getEndTime()).thenReturn(new java.util.Date());
        lenient().when(histTask.getProcessInstanceId()).thenReturn("proc-123");
        lenient().when(historicTaskQuery.singleResult()).thenReturn(histTask);

        HistoricVariableInstance histOfficer = mock(HistoricVariableInstance.class);
        lenient().when(histOfficer.getValue()).thenReturn("officer1"); // Completed by officer1
        HistoricVariableInstance histDecision = mock(HistoricVariableInstance.class);
        lenient().when(histDecision.getValue()).thenReturn("APPROVE");

        HistoricVariableInstanceQuery q1 = mock(HistoricVariableInstanceQuery.class);
        HistoricVariableInstanceQuery q2 = mock(HistoricVariableInstanceQuery.class);
        HistoricVariableInstanceQuery q3 = mock(HistoricVariableInstanceQuery.class);

        lenient().when(historyService.createHistoricVariableInstanceQuery()).thenReturn(q1, q2, q3);
        
        lenient().when(q1.processInstanceId("proc-123")).thenReturn(q1);
        lenient().when(q1.variableName("officerId")).thenReturn(q1);
        lenient().when(q1.singleResult()).thenReturn(histOfficer);

        lenient().when(q2.processInstanceId("proc-123")).thenReturn(q2);
        lenient().when(q2.variableName("officerDecision")).thenReturn(q2);
        lenient().when(q2.singleResult()).thenReturn(histDecision);

        lenient().when(q3.processInstanceId("proc-123")).thenReturn(q3);
        lenient().when(q3.variableName("applicationId")).thenReturn(q3);
        lenient().when(q3.singleResult()).thenReturn(null);

        // officer2 tries to submit decision
        assertThrows(TaskAlreadyCompletedException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer2", "DEPT-SKILLS", "APPROVE", null)
        );
    }
}
