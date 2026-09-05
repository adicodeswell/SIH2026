package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.ApplicationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApplicationServiceClient {

    private final RestTemplate restTemplate;
    private final String applicationServiceUrl;
    private final ServiceTokenProvider serviceTokenProvider;

    public ApplicationServiceClient(
            RestTemplate restTemplate,
            @Value("${mahasetu.application-service.url}") String applicationServiceUrl,
            ServiceTokenProvider serviceTokenProvider) {
        this.restTemplate = restTemplate;
        this.applicationServiceUrl = applicationServiceUrl;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    private static final int MAX_ATTEMPTS = 3;

    public ApplicationResponse getApplication(String applicationId) {
        String url = applicationServiceUrl + "/api/v1/applications/" + applicationId;
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", serviceTokenProvider.getAuthorizationHeader());
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            try {
                return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, ApplicationResponse.class).getBody();
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                throw e; // Fail fast on 4xx
            } catch (org.springframework.web.client.HttpServerErrorException | org.springframework.web.client.ResourceAccessException e) {
                if (attempts >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
        return null;
    }
}
