package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.ApplicationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApplicationServiceClient {

    private final RestTemplate restTemplate;
    private final String applicationServiceUrl;

    public ApplicationServiceClient(
            RestTemplate restTemplate,
            @Value("${mahasetu.application-service.url}") String applicationServiceUrl) {
        this.restTemplate = restTemplate;
        this.applicationServiceUrl = applicationServiceUrl;
    }

    public ApplicationResponse getApplication(String applicationId) {
        String url = applicationServiceUrl + "/api/v1/applications/" + applicationId;
        return restTemplate.getForObject(url, ApplicationResponse.class);
    }
}
