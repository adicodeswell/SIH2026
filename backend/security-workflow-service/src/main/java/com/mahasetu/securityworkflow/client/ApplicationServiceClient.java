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

    public ApplicationResponse getApplication(String applicationId) {
        String url = applicationServiceUrl + "/api/v1/applications/" + applicationId;
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", serviceTokenProvider.getAuthorizationHeader());
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
        return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, ApplicationResponse.class).getBody();
    }
}
