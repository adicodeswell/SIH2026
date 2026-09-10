import re

def run():
    # 1. Update OfficerTaskServiceTest
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java', 'r') as f:
        content = f.read()

    # The setup method or test needs smart mock for historyService
    smart_mock = """
        org.camunda.bpm.engine.history.HistoricVariableInstanceQuery varQuery = org.mockito.Mockito.mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
        lenient().when(historyService.createHistoricVariableInstanceQuery()).thenReturn(varQuery);
        lenient().when(varQuery.processInstanceId(anyString())).thenReturn(varQuery);
        lenient().when(varQuery.variableName(anyString())).thenAnswer(inv -> {
            String varName = inv.getArgument(0);
            org.camunda.bpm.engine.history.HistoricVariableInstanceQuery mockQuery = org.mockito.Mockito.mock(org.camunda.bpm.engine.history.HistoricVariableInstanceQuery.class);
            org.camunda.bpm.engine.history.HistoricVariableInstance mockVar = org.mockito.Mockito.mock(org.camunda.bpm.engine.history.HistoricVariableInstance.class);
            
            if ("applicationId".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("APP-1001");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("serviceCode".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("TEST_SVC");
                lenient().when(mockQuery.singleResult()).thenReturn(mockVar);
            } else if ("officerId".equals(varName)) {
                lenient().when(mockVar.getValue()).thenReturn("officer1");
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
            "TEST_SVC", java.util.Set.of(), "verification", "NONE", "SKILLS", java.util.Set.of(), java.util.Set.of()
        );
        lenient().when(consentPolicyService.getPolicy("TEST_SVC")).thenReturn(policy);
"""
    
    # We can inject this into testCompleteOfficerDecision_DuplicateSameDecision_ReturnsIdempotentResponse
    idx = content.find("void testCompleteOfficerDecision_DuplicateSameDecision_ReturnsIdempotentResponse() {")
    if idx != -1:
        insert_idx = content.find("{", idx) + 1
        content = content[:insert_idx] + smart_mock + content[insert_idx:]

    idx = content.find("void testCompleteOfficerDecision_AlreadyCompleted_DifferentDecision_ThrowsTaskAlreadyCompletedException() {")
    if idx != -1:
        insert_idx = content.find("{", idx) + 1
        content = content[:insert_idx] + smart_mock + content[insert_idx:]
        
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java', 'w') as f:
        f.write(content)

    # 2. Update OfficerReviewHardeningTest
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewHardeningTest.java', 'r') as f:
        content2 = f.read()
        
    idx = content2.find("void testCompleteDecision_CompletedTask_SameOfficerSameDecision_IdempotentResponse() {")
    if idx != -1:
        insert_idx = content2.find("{", idx) + 1
        content2 = content2[:insert_idx] + smart_mock + content2[insert_idx:]

    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewHardeningTest.java', 'w') as f:
        f.write(content2)

run()
