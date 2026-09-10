import re

def run():
    with open('backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/OfficerTaskService.java', 'r') as f:
        content = f.read()

    resolve_historical_code = """
    private TaskContext resolveHistoricalTaskContext(String processInstanceId) {
        org.camunda.bpm.engine.history.HistoricVariableInstance appVar = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName("applicationId")
                .singleResult();
                
        org.camunda.bpm.engine.history.HistoricVariableInstance svcVar = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName("serviceCode")
                .singleResult();

        if (appVar == null || svcVar == null) {
            throw new InvalidTaskOperationException("Task context is invalid");
        }

        Object applicationIdValue = appVar.getValue();
        Object serviceCodeValue = svcVar.getValue();

        if (!(applicationIdValue instanceof String applicationId) || applicationId.isBlank() ||
            !(serviceCodeValue instanceof String serviceCode) || serviceCode.isBlank()) {
            throw new InvalidTaskOperationException("Task context is invalid");
        }

        String normalizedServiceCode = serviceCode.trim().toUpperCase(java.util.Locale.ROOT);
        com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy policy = consentPolicyService.getPolicy(normalizedServiceCode);
        if (policy == null) {
            throw new InvalidTaskOperationException("Task context is invalid");
        }

        return new TaskContext(applicationId.trim(), normalizedServiceCode, policy);
    }
"""

    # Add the method right after resolveTaskContext
    if "private TaskContext resolveHistoricalTaskContext" not in content:
        content = content.replace(
            "return new TaskContext(applicationId.trim(), normalizedServiceCode, policy);\n    }",
            "return new TaskContext(applicationId.trim(), normalizedServiceCode, policy);\n    }\n" + resolve_historical_code
        )

    # Now replace the idempotency logic
    old_idemp = """            if (historicTask != null && historicTask.getEndTime() != null) {
                log.warn("Task {} exists in history and has already completed", taskId);
                org.camunda.bpm.engine.history.HistoricVariableInstance histOfficer = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicTask.getProcessInstanceId())
                        .variableName("officerId")
                        .singleResult();
                org.camunda.bpm.engine.history.HistoricVariableInstance histDecision = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicTask.getProcessInstanceId())
                        .variableName("officerDecision")
                        .singleResult();
                org.camunda.bpm.engine.history.HistoricVariableInstance histAppId = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicTask.getProcessInstanceId())
                        .variableName("applicationId")
                        .singleResult();

                if (histOfficer != null && officerId.equals(histOfficer.getValue()) &&
                    histDecision != null && normalizedDecision.equals(histDecision.getValue())) {
                    log.info("Duplicate decision detected for completed task {}: returning idempotent response", taskId);
                    return new OfficerDecisionResponse(
                            taskId,
                            histAppId != null ? (String) histAppId.getValue() : null,
                            normalizedDecision,
                            officerId,
                            reason,
                            LocalDateTime.now(),
                            "COMPLETED"
                    );
                }
                throw new TaskAlreadyCompletedException(taskId);
            }"""

    new_idemp = """            if (historicTask != null && historicTask.getEndTime() != null) {
                log.warn("Task {} exists in history and has already completed", taskId);
                
                String procInstId = historicTask.getProcessInstanceId();
                if (procInstId == null || procInstId.isBlank()) {
                    throw new InvalidTaskOperationException("Task context is invalid");
                }
                
                TaskContext ctx = resolveHistoricalTaskContext(procInstId);
                String applicationDepartment = ctx.policy().getRequestingDepartmentId();
                
                if (applicationDepartment == null || applicationDepartment.trim().isEmpty()) {
                    throw new org.springframework.security.access.AccessDeniedException("Application department is not configured");
                }
                String authorizedDepartment = applicationDepartment.trim().toUpperCase(java.util.Locale.ROOT);
                if (!authorizedDepartment.equals(officerDepartment != null ? officerDepartment.trim().toUpperCase(java.util.Locale.ROOT) : null)) {
                    throw new org.springframework.security.access.AccessDeniedException("Officer is not authorized");
                }

                org.camunda.bpm.engine.history.HistoricVariableInstance histOfficer = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(procInstId)
                        .variableName("officerId")
                        .singleResult();
                org.camunda.bpm.engine.history.HistoricVariableInstance histDecision = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(procInstId)
                        .variableName("officerDecision")
                        .singleResult();

                if (histOfficer != null && officerId.equals(histOfficer.getValue()) &&
                    histDecision != null && normalizedDecision.equals(histDecision.getValue())) {
                    log.info("Duplicate decision detected for completed task {}: returning idempotent response", taskId);
                    return new OfficerDecisionResponse(
                            taskId,
                            ctx.applicationId(),
                            normalizedDecision,
                            officerId,
                            reason,
                            LocalDateTime.now(),
                            "COMPLETED"
                    );
                }
                throw new TaskAlreadyCompletedException(taskId);
            }"""

    if old_idemp in content:
        content = content.replace(old_idemp, new_idemp)
        with open('backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/OfficerTaskService.java', 'w') as f:
            f.write(content)
        print("Fix 2 applied successfully")
    else:
        print("Could not find the target code for Fix 2")

run()
