package com.mahasetu.securityworkflow.service.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.client.InteroperabilityClient;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
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
        
        @SuppressWarnings("unchecked")
        List<String> allowedScopes = (List<String>) execution.getVariable("allowedScopes");

        log.info("[INTEROP_EVENT] InteroperabilityWorker: fetching data for applicationId={}, citizenId={}, scopes={}", applicationId, citizenId, allowedScopes);

        if (allowedScopes == null || allowedScopes.isEmpty()) {
            throw new BpmnError("INTEROP_FETCH_FAILED", "No allowed scopes provided by consent boundary");
        }

        try {
            List<SourceDataResult> data = interoperabilityClient.fetchScopedData(citizenId, allowedScopes);

            if (data == null || data.isEmpty()) {
                log.warn("[INTEROP_EVENT] InteroperabilityWorker: No records found for citizenId={}", citizenId);
                execution.setVariable("failureReason", "Citizen record not found in Interoperability Service");
                throw new BpmnError("INTEROP_FETCH_FAILED", "No citizen data found");
            }

            // Serialize the data to store it as a JSON string in Camunda variables
            String jsonData = objectMapper.writeValueAsString(data);

            execution.setVariable("interoperabilityResult", jsonData);
            execution.setVariable("workflowStatus", "SUCCESS");

            log.info("[INTEROP_EVENT] InteroperabilityWorker: completed for applicationId={}, citizenId={}", applicationId, citizenId);
        } catch (BpmnError e) {
            throw e;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String reason = "Interoperability client error (" + e.getStatusCode() + "): " + e.getMessage();
            log.error("[INTEROP_EVENT] InteroperabilityWorker: client error for applicationId={}, error={}", applicationId, reason);
            execution.setVariable("failureReason", reason);
            throw new BpmnError("INTEROP_FETCH_FAILED", reason);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            String reason = "Interoperability downstream server error (" + e.getStatusCode() + "): " + e.getMessage();
            log.error("[INTEROP_EVENT] InteroperabilityWorker: server error for applicationId={}, error={}", applicationId, reason);
            execution.setVariable("failureReason", reason);
            throw new BpmnError("INTEROP_FETCH_FAILED", reason);
        } catch (Exception e) {
            String reason = "Failed to fetch data from Interoperability Service: " + e.getMessage();
            log.error("[INTEROP_EVENT] InteroperabilityWorker: failed for applicationId={}, citizenId={}, error={}",
                    applicationId, citizenId, reason);
            execution.setVariable("failureReason", reason);
            throw new BpmnError("INTEROP_FETCH_FAILED", reason);
        }
    }
}
