package com.mahasetu.securityworkflow.service.worker;

import com.mahasetu.securityworkflow.client.WorkflowStatusClient;
import com.mahasetu.securityworkflow.dto.WorkflowStatusCallback;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Camunda JavaDelegate that sends workflow status callbacks to Application Service.
 * Reads workflowStatus, applicationId, and failureReason from process variables.
 * Callback failures are logged but do not cause workflow failure.
 */
@Component
public class StatusCallbackWorker implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(StatusCallbackWorker.class);

    private final WorkflowStatusClient workflowStatusClient;

    public StatusCallbackWorker(WorkflowStatusClient workflowStatusClient) {
        this.workflowStatusClient = workflowStatusClient;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String applicationId = (String) execution.getVariable("applicationId");
        String workflowStatus = (String) execution.getVariable("workflowStatus");
        String failureReason = (String) execution.getVariable("failureReason");
        String officerId = (String) execution.getVariable("officerId");
        String processInstanceId = execution.getProcessInstanceId();

        log.info("[WORKFLOW_EVENT] StatusCallbackWorker executing: applicationId={}, processInstanceId={}, workflowStatus={}, officerId={}",
                applicationId, processInstanceId, workflowStatus, officerId);

        WorkflowStatusCallback callback = new WorkflowStatusCallback(
                applicationId,
                processInstanceId,
                workflowStatus,
                failureReason,
                officerId
        );

        workflowStatusClient.sendStatusCallback(callback);
    }
}
