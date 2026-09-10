import re

def run():
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java', 'r') as f:
        content = f.read()

    # We need to rename varQuery to smartVarQuery in the injected block
    content = content.replace("HistoricVariableInstanceQuery varQuery = org.mockito.Mockito.mock", "HistoricVariableInstanceQuery smartVarQuery = org.mockito.Mockito.mock")
    content = content.replace("historyService.createHistoricVariableInstanceQuery()).thenReturn(varQuery)", "historyService.createHistoricVariableInstanceQuery()).thenReturn(smartVarQuery)")
    content = content.replace("when(varQuery.", "when(smartVarQuery.")
    
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java', 'w') as f:
        f.write(content)

    # Let's do the same for OfficerReviewHardeningTest.java just in case
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewHardeningTest.java', 'r') as f:
        content2 = f.read()

    content2 = content2.replace("HistoricVariableInstanceQuery varQuery = org.mockito.Mockito.mock", "HistoricVariableInstanceQuery smartVarQuery = org.mockito.Mockito.mock")
    content2 = content2.replace("historyService.createHistoricVariableInstanceQuery()).thenReturn(varQuery)", "historyService.createHistoricVariableInstanceQuery()).thenReturn(smartVarQuery)")
    content2 = content2.replace("when(varQuery.", "when(smartVarQuery.")
    
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewHardeningTest.java', 'w') as f:
        f.write(content2)

run()
