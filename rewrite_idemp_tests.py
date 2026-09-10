import re

def run():
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewAdversarialTest.java', 'r') as f:
        content = f.read()

    # Find where idempotency tests start (around line 203)
    marker = "// 5. Idempotent Retry Authorization"
    idx = content.find(marker)
    if idx == -1:
        print("Marker not found")
        return
        
    prefix = content[:idx]
    
    new_tests = """// 5. Idempotent Retry Authorization

    private void setupHistoricalContext(String procInstId, String appId, String svcCode, String officerId, String decision) {
        lenient().when(taskQuery.singleResult()).thenReturn(null);

        HistoricTaskInstance histTask = mock(HistoricTaskInstance.class);
        lenient().when(histTask.getEndTime()).thenReturn(new java.util.Date());
        lenient().when(histTask.getProcessInstanceId()).thenReturn(procInstId);
        lenient().when(historicTaskQuery.singleResult()).thenReturn(histTask);

        org.camunda.bpm.engine.history.HistoricVariableInstanceQuery varQuery = mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
        lenient().when(historyService.createHistoricVariableInstanceQuery()).thenReturn(varQuery);
        lenient().when(varQuery.processInstanceId(anyString())).thenReturn(varQuery);

        // We use answer to return different values based on variableName
        lenient().when(varQuery.variableName(anyString())).thenAnswer(inv -> {
            String varName = inv.getArgument(0);
            org.camunda.bpm.engine.history.HistoricVariableInstanceQuery mockQuery = mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
            HistoricVariableInstance mockVar = mock(HistoricVariableInstance.class);
            
            if ("applicationId".equals(varName) && appId != null) {
                lenient().when(mockVar.getValue()).thenReturn(appId);
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("serviceCode".equals(varName) && svcCode != null) {
                lenient().when(mockVar.getValue()).thenReturn(svcCode);
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerId".equals(varName) && officerId != null) {
                lenient().when(mockVar.getValue()).thenReturn(officerId);
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerDecision".equals(varName) && decision != null) {
                lenient().when(mockVar.getValue()).thenReturn(decision);
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else {
                lenient().when(mockQuery.singleResult()).thenReturn(null);
            }
            return mockQuery;
        });
    }

    // TEST A
    @Test
    void testDecision_CompletedTaskSameOfficer_AllowedIdempotent() {
        setupHistoricalContext("proc-123", "APP-100", "SKILL_BENEFIT", "officer1", "APPROVE");
        OfficerDecisionResponse resp = officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null);
        assertEquals("COMPLETED", resp.getStatus());
        assertEquals("APPROVE", resp.getDecision());
        assertEquals("APP-100", resp.getApplicationId());
    }

    // TEST B
    @Test
    void testDecision_CompletedTaskSameOfficer_WrongDepartment_Throws() {
        setupHistoricalContext("proc-123", "APP-100", "SKILL_BENEFIT", "officer1", "APPROVE");
        assertThrows(AccessDeniedException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-EDU", "APPROVE", null)
        );
        verify(taskService, never()).complete(anyString(), any());
    }

    // TEST C
    @Test
    void testDecision_CompletedTaskDifferentOfficer_Throws() {
        setupHistoricalContext("proc-123", "APP-100", "SKILL_BENEFIT", "officer1", "APPROVE");
        assertThrows(TaskAlreadyCompletedException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer2", "DEPT-SKILLS", "APPROVE", null)
        );
    }

    // TEST D
    @Test
    void testDecision_CompletedTaskSameOfficer_DifferentDecision_Throws() {
        setupHistoricalContext("proc-123", "APP-100", "SKILL_BENEFIT", "officer1", "APPROVE");
        assertThrows(TaskAlreadyCompletedException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "REJECT", "Reason")
        );
    }

    // TEST E
    @Test
    void testDecision_CompletedTask_MissingApplicationId_Throws() {
        setupHistoricalContext("proc-123", null, "SKILL_BENEFIT", "officer1", "APPROVE");
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
    }

    // TEST F
    @Test
    void testDecision_CompletedTask_MissingServiceCode_Throws() {
        setupHistoricalContext("proc-123", "APP-100", null, "officer1", "APPROVE");
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
    }

    // TEST G
    @Test
    void testDecision_CompletedTask_BlankApplicationId_Throws() {
        setupHistoricalContext("proc-123", "   ", "SKILL_BENEFIT", "officer1", "APPROVE");
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
    }

    // TEST H
    @Test
    void testDecision_CompletedTask_BlankServiceCode_Throws() {
        setupHistoricalContext("proc-123", "APP-100", "  ", "officer1", "APPROVE");
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
    }

    // TEST I
    @Test
    void testDecision_CompletedTask_UnknownServiceCode_Throws() {
        setupHistoricalContext("proc-123", "APP-100", "UNKNOWN_SVC", "officer1", "APPROVE");
        lenient().when(consentPolicyService.getPolicy("UNKNOWN_SVC")).thenReturn(null);
        assertThrows(InvalidTaskOperationException.class, () -> 
            officerTaskService.completeOfficerDecision("task-123", "officer1", "DEPT-SKILLS", "APPROVE", null)
        );
    }
}
"""
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewAdversarialTest.java', 'w') as f:
        f.write(prefix + new_tests)

run()
