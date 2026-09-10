import re

def run():
    setup_addition = """
        org.camunda.bpm.engine.task.TaskQuery mockTaskQuery = mock(org.camunda.bpm.engine.task.TaskQuery.class);
        lenient().when(taskService.createTaskQuery()).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.taskId(anyString())).thenReturn(mockTaskQuery);
        lenient().when(mockTaskQuery.singleResult()).thenReturn(null);
    """

    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java', 'r') as f:
        content = f.read()

    content = content.replace('HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);', setup_addition + '\n        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);')
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerTaskServiceTest.java', 'w') as f:
        f.write(content)

    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewHardeningTest.java', 'r') as f:
        content = f.read()

    content = content.replace('HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);', setup_addition + '\n        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);')
    with open('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/OfficerReviewHardeningTest.java', 'w') as f:
        f.write(content)

run()
