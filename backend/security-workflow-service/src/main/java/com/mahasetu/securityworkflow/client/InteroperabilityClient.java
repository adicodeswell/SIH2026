package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class InteroperabilityClient {

    private final RestTemplate restTemplate;
    private final String interoperabilityServiceUrl;
    private final String token;

    public InteroperabilityClient(
            RestTemplate restTemplate,
            @Value("${mahasetu.interoperability-service.url}") String interoperabilityServiceUrl,
            @Value("${mahasetu.interoperability-service.token}") String token) {
        this.restTemplate = restTemplate;
        this.interoperabilityServiceUrl = interoperabilityServiceUrl;
        this.token = token;
    }

    private static final int MAX_ATTEMPTS = 3;

    public List<CanonicalCitizenData> fetchAllData(String citizenId) {
        String url = interoperabilityServiceUrl + "/api/v1/interop/fetch/all/" + citizenId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            try {
                ResponseEntity<List<CanonicalCitizenData>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<CanonicalCitizenData>>() {}
                );
                return response.getBody();
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // Permanent 4xx client error (e.g. 404, 401): do not retry
                throw e;
            } catch (org.springframework.web.client.HttpServerErrorException | org.springframework.web.client.ResourceAccessException e) {
                if (attempts >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
        return null;
    }
}
