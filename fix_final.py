import re

def rewrite_test_method(filepath, test_method_name, setup_logic, test_logic):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find the test method
    pattern = r'@Test\s+void\s+' + test_method_name + r'\s*\(\)\s*\{.*?\n    \}'
    match = re.search(pattern, content, re.DOTALL)
    
    if match:
        new_method = f"""@Test
    void {test_method_name}() {{
        {setup_logic}
        {test_logic}
    }}"""
        content = content[:match.start()] + new_method + content[match.end():]
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Replaced {test_method_name} in {filepath}")
    else:
        print(f"Could not find {test_method_name} in {filepath}")


setup_logic_ots = """
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
"""

# For testCompleteOfficerDecision_AlreadyCompleted_DifferentDecision_ThrowsTaskAlreadyCompletedException
rewrite_test_method(
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java',
    'testCompleteOfficerDecision_AlreadyCompleted_DifferentDecision_ThrowsTaskAlreadyCompletedException',
    setup_logic_ots,
    """
        assertThrows(TaskAlreadyCompletedException.class, () ->
                officerTaskService.completeOfficerDecision("task-done", "officer_1", "SKILLS", "REJECT", "Reason"));
    """
)

# For testCompleteOfficerDecision_DuplicateSameDecision_ReturnsIdempotentResponse
rewrite_test_method(
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java',
    'testCompleteOfficerDecision_DuplicateSameDecision_ReturnsIdempotentResponse',
    setup_logic_ots,
    """
        com.mahasetu.securityworkflow.dto.OfficerDecisionResponse response = officerTaskService.completeOfficerDecision("task-done", "officer_1", "SKILLS", "APPROVE", null);
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("APPROVE", response.getDecision());
    """
)


# -------------------------------------------------------------
# For OfficerReviewHardeningTest
# -------------------------------------------------------------
setup_logic_orh = """
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
                lenient().when(mockVar.getValue()).thenReturn("APP-100");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("serviceCode".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("SKILLS");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerId".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn(OFFICER_1);
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
"""

rewrite_test_method(
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewHardeningTest.java',
    'testCompleteDecision_CompletedTask_SameOfficerSameDecision_IdempotentResponse',
    setup_logic_orh,
    """
        com.mahasetu.securityworkflow.dto.OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(TASK_ID, OFFICER_1, "SKILLS", "APPROVE", null);
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("APPROVE", response.getDecision());
        assertEquals("APP-100", response.getApplicationId());
    """
)
run()
