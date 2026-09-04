package com.mahasetu.securityworkflow.service.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.client.InteroperabilityClient;
import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InteroperabilityWorker implements JavaDelegate {

    private final InteroperabilityClient interoperabilityClient;
    private final ObjectMapper objectMapper;

    public InteroperabilityWorker(InteroperabilityClient interoperabilityClient, ObjectMapper objectMapper) {
        this.interoperabilityClient = interoperabilityClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String citizenId = (String) execution.getVariable("citizenId");

        try {
            List<CanonicalCitizenData> data = interoperabilityClient.fetchAllData(citizenId);
            
            // Serialize the data to store it as a JSON string in Camunda variables
            String jsonData = objectMapper.writeValueAsString(data);
            
            execution.setVariable("interoperabilityResult", jsonData);
            execution.setVariable("workflowStatus", "SUCCESS");
        } catch (Exception e) {
            execution.setVariable("workflowStatus", "INTEROPERABILITY_FAILURE");
            throw e; // Let Camunda handle the exception or we could just set a variable and continue
        }
    }
}
