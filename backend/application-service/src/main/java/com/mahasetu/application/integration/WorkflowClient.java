package com.mahasetu.application.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class WorkflowClient {

    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public WorkflowClient(@Value("${integration.workflow-service.url:http://workflow-service}") String baseUrl,
                          ServiceTokenProvider serviceTokenProvider) {
        // Use SimpleClientHttpRequestFactory to avoid JDK HttpClient HTTP/2 issues
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.serviceTokenProvider = serviceTokenProvider;
    }

    public void startWorkflow(String applicationId, String workflowKey) {
        restClient.post()
                .uri("/internal/v1/workflows")
                .header("Authorization", serviceTokenProvider.getAuthorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
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
                .header("Authorization", serviceTokenProvider.getAuthorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "eventType", eventType,
                        "newStatus", newStatus,
                        "metadata", metadata != null ? metadata : ""
                ))
                .retrieve()
                .toBodilessEntity();
    }
}

