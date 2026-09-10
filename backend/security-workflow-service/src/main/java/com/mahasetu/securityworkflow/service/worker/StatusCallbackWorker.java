package com.mahasetu.securityworkflow.service.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.client.WorkflowStatusClient;
import com.mahasetu.securityworkflow.dto.WorkflowStatusCallback;
import com.mahasetu.securityworkflow.dto.verification.VerificationResult;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StatusCallbackWorker implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(StatusCallbackWorker.class);

    private final WorkflowStatusClient workflowStatusClient;
    private final ObjectMapper objectMapper;

    public StatusCallbackWorker(WorkflowStatusClient workflowStatusClient, ObjectMapper objectMapper) {
        this.workflowStatusClient = workflowStatusClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String applicationId = (String) execution.getVariable("applicationId");
        String workflowStatus = (String) execution.getVariable("workflowStatus");
        String failureReason = (String) execution.getVariable("failureReason");
        String officerId = (String) execution.getVariable("officerId");
        String processInstanceId = execution.getProcessInstanceId();
        
        String verificationResultJson = (String) execution.getVariable("verificationResult");

        log.info("[WORKFLOW_EVENT] StatusCallbackWorker executing: applicationId={}, processInstanceId={}, workflowStatus={}, officerId={}",
                applicationId, processInstanceId, workflowStatus, officerId);

        WorkflowStatusCallback callback = new WorkflowStatusCallback(
                applicationId,
                processInstanceId,
                workflowStatus,
                failureReason,
                officerId
        );
        
        if (verificationResultJson != null) {
            VerificationResult fullResult = objectMapper.readValue(verificationResultJson, VerificationResult.class);
            Map<String, Object> minimized = new HashMap<>();
            minimized.put("overallStatus", fullResult.getOverallStatus());
            minimized.put("reasons", fullResult.getReasons());
            minimized.put("sourceResults", fullResult.getSourceResults());
            // Intentionally omitting fieldResults to minimize callback payload.
            callback.setVerificationData(minimized);
        }

        try {
            workflowStatusClient.sendStatusCallback(callback);
            log.info("[WORKFLOW_EVENT] Callback successfully sent for application {}", applicationId);
        } catch (Exception e) {
            log.warn("[WORKFLOW_EVENT] Callback failed for application {}, but workflow will continue: {}", applicationId, e.getMessage());
            // Do NOT throw exception. Failure here must not stop the main workflow progression (e.g. reaching Officer Review).
        }
    }
}
