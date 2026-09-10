package com.mahasetu.application.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class WorkflowAuditClient {

    private static final Logger log = LoggerFactory.getLogger(WorkflowAuditClient.class);

    private final RestTemplate restTemplate;
    private final ServiceTokenProvider tokenProvider;
    private final String workflowServiceUrl;

    public WorkflowAuditClient(RestTemplate restTemplate,
                               ServiceTokenProvider tokenProvider,
                               @Value("${integration.workflow-service.url}") String workflowServiceUrl) {
        this.restTemplate = restTemplate;
        this.tokenProvider = tokenProvider;
        this.workflowServiceUrl = workflowServiceUrl;
    }

    public List<Map<String, Object>> getApplicationAuditLogs(String applicationId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", tokenProvider.getAuthorizationHeader());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    workflowServiceUrl + "/internal/v1/audit/applications/" + applicationId,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch audit logs for application {}", applicationId, e);
            throw new RuntimeException("Failed to fetch workflow audit logs", e);
        }
    }
}
