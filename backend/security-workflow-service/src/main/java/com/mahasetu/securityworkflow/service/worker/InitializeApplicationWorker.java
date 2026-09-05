package com.mahasetu.securityworkflow.service.worker;

import com.mahasetu.securityworkflow.client.ApplicationServiceClient;
import com.mahasetu.securityworkflow.dto.ApplicationResponse;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InitializeApplicationWorker implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(InitializeApplicationWorker.class);

    private final ApplicationServiceClient applicationServiceClient;

    public InitializeApplicationWorker(ApplicationServiceClient applicationServiceClient) {
        this.applicationServiceClient = applicationServiceClient;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String applicationId = (String) execution.getVariable("applicationId");

        log.info("[WORKFLOW_EVENT] InitializeApplicationWorker: starting for applicationId={}", applicationId);

        if (applicationId == null || applicationId.trim().isEmpty()) {
            execution.setVariable("failureReason", "applicationId is missing");
            throw new BpmnError("APPLICATION_FETCH_FAILED", "applicationId is missing in execution variables");
        }

        try {
            ApplicationResponse response = applicationServiceClient.getApplication(applicationId);

            if (response == null) {
                execution.setVariable("failureReason", "Application not found: " + applicationId);
                throw new BpmnError("APPLICATION_FETCH_FAILED", "Application not found for id: " + applicationId);
            }

            execution.setVariable("citizenId", response.getCitizenId());
            execution.setVariable("serviceCode", response.getServiceCode());

            log.info("[WORKFLOW_EVENT] InitializeApplicationWorker: completed for applicationId={}, citizenId={}, serviceCode={}",
                    applicationId, response.getCitizenId(), response.getServiceCode());
        } catch (BpmnError e) {
            throw e; // Re-throw BpmnErrors — they are handled by BPMN boundary events
        } catch (Exception e) {
            log.error("[WORKFLOW_EVENT] InitializeApplicationWorker: failed to fetch application applicationId={}, error={}",
                    applicationId, e.getMessage());
            execution.setVariable("failureReason", "Failed to fetch application from Application Service");
            throw new BpmnError("APPLICATION_FETCH_FAILED", "Error fetching application: " + e.getMessage());
        }
    }
}
