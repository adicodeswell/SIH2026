package com.mahasetu.application.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ConsentClient {

    private final RestClient restClient;
    private final ServiceTokenProvider tokenProvider;

    public ConsentClient(@Value("${integration.workflow-service.url:http://security-workflow-service:8083}") String baseUrl,
                         ServiceTokenProvider tokenProvider) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.tokenProvider = tokenProvider;
    }

    public boolean hasConsentForApplication(String applicationId) {
        try {
            org.springframework.http.ResponseEntity<Boolean> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/v1/consents/check")
                            .queryParam("applicationId", applicationId)
                            .build())
                    .header("Authorization", tokenProvider.getAuthorizationHeader())
                    .retrieve()
                    .toEntity(Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            return false;
        }
    }
}
