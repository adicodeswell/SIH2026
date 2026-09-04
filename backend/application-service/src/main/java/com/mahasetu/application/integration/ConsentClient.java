package com.mahasetu.application.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ConsentClient {

    private final RestClient restClient;

    public ConsentClient(@Value("${integration.consent-service.url:http://consent-service}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean checkConsent(String citizenId, String dataScope, String purpose) {
        try {
            restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/v1/consents/check")
                            .queryParam("citizenId", citizenId)
                            .queryParam("dataScope", dataScope)
                            .queryParam("purpose", purpose)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
