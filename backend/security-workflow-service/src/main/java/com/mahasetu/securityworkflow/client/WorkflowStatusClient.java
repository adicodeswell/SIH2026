package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.WorkflowStatusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for sending workflow status callbacks to Application Service.
 * Callback failures are logged but do not propagate — preventing recursive workflow failures.
 */
@Component
public class WorkflowStatusClient {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStatusClient.class);

    private final RestTemplate restTemplate;
    private final String applicationServiceUrl;
    private final ServiceTokenProvider serviceTokenProvider;

    public WorkflowStatusClient(
            RestTemplate restTemplate,
            @Value("${mahasetu.application-service.url}") String applicationServiceUrl,
            ServiceTokenProvider serviceTokenProvider) {
        this.restTemplate = restTemplate;
        this.applicationServiceUrl = applicationServiceUrl;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    /**
     * Sends a workflow status callback to Application Service.
     * Uses the internal callback endpoint for workflow status updates.
     * Callback failures are caught and logged — they must not cause recursive workflow failures.
     *
     * @param callback the callback payload
     */
    public void sendStatusCallback(WorkflowStatusCallback callback) {
        String url = applicationServiceUrl + "/internal/v1/applications/" + callback.getApplicationId() + "/workflow-status";

        log.info("Sending workflow status callback: applicationId={}, processInstanceId={}, status={}",
                callback.getApplicationId(), callback.getProcessInstanceId(), callback.getStatus());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", serviceTokenProvider.getAuthorizationHeader());
            HttpEntity<WorkflowStatusCallback> entity = new HttpEntity<>(callback, headers);

            restTemplate.postForEntity(url, entity, Void.class);

            log.info("Workflow status callback sent successfully: applicationId={}, status={}",
                    callback.getApplicationId(), callback.getStatus());
        } catch (Exception e) {
            // Callback failure must NOT cause recursive workflow failure
            log.error("Failed to send workflow status callback: applicationId={}, status={}, error={}",
                    callback.getApplicationId(), callback.getStatus(), e.getMessage());
        }
    }
}
