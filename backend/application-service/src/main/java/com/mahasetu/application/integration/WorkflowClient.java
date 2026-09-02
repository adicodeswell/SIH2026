package com.mahasetu.application.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class WorkflowClient {

    private final RestClient restClient;

    public WorkflowClient(@Value("${integration.workflow-service.url:http://workflow-service}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void startWorkflow(String applicationId, String workflowKey) {
        restClient.post()
                .uri("/internal/v1/workflows")
                .body(Map.of(
                        "applicationId", applicationId,
                        "workflowKey", workflowKey
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public void emitWorkflowEvent(String applicationId, String eventType, String newStatus, String metadata) {
        restClient.post()
                .uri("/internal/v1/applications/{id}/workflow-events", applicationId)
                .body(Map.of(
                        "eventType", eventType,
                        "newStatus", newStatus,
                        "metadata", metadata != null ? metadata : ""
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
