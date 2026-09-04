package com.mahasetu.securityworkflow.service.worker;

import com.mahasetu.securityworkflow.client.ApplicationServiceClient;
import com.mahasetu.securityworkflow.dto.ApplicationResponse;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class InitializeApplicationWorker implements JavaDelegate {

    private final ApplicationServiceClient applicationServiceClient;

    public InitializeApplicationWorker(ApplicationServiceClient applicationServiceClient) {
        this.applicationServiceClient = applicationServiceClient;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String applicationId = (String) execution.getVariable("applicationId");
        
        if (applicationId == null || applicationId.trim().isEmpty()) {
            throw new IllegalArgumentException("applicationId is missing in execution variables");
        }

        ApplicationResponse response = applicationServiceClient.getApplication(applicationId);
        
        if (response == null) {
            throw new RuntimeException("Application not found for id: " + applicationId);
        }

        execution.setVariable("citizenId", response.getCitizenId());
        execution.setVariable("serviceCode", response.getServiceCode());
    }
}
