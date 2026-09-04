package com.mahasetu.securityworkflow.service.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.client.InteroperabilityClient;
import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InteroperabilityWorker implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(InteroperabilityWorker.class);

    private final InteroperabilityClient interoperabilityClient;
    private final ObjectMapper objectMapper;

    public InteroperabilityWorker(InteroperabilityClient interoperabilityClient, ObjectMapper objectMapper) {
        this.interoperabilityClient = interoperabilityClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String citizenId = (String) execution.getVariable("citizenId");
        String applicationId = (String) execution.getVariable("applicationId");

        log.info("InteroperabilityWorker: fetching data for applicationId={}, citizenId={}", applicationId, citizenId);

        try {
            List<CanonicalCitizenData> data = interoperabilityClient.fetchAllData(citizenId);

            // Serialize the data to store it as a JSON string in Camunda variables
            String jsonData = objectMapper.writeValueAsString(data);

            execution.setVariable("interoperabilityResult", jsonData);
            execution.setVariable("workflowStatus", "SUCCESS");

            log.info("InteroperabilityWorker: completed for applicationId={}, citizenId={}", applicationId, citizenId);
        } catch (Exception e) {
            log.error("InteroperabilityWorker: failed for applicationId={}, citizenId={}, error={}",
                    applicationId, citizenId, e.getMessage());
            execution.setVariable("failureReason", "Failed to fetch data from Interoperability Service");
            throw new BpmnError("INTEROP_FETCH_FAILED", "Error fetching interoperability data: " + e.getMessage());
        }
    }
}
