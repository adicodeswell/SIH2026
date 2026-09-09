package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.ScopedInteropRequest;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
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
    private final ServiceTokenProvider serviceTokenProvider;

    public InteroperabilityClient(
            RestTemplate restTemplate,
            @Value("${mahasetu.interoperability-service.url}") String interoperabilityServiceUrl,
            ServiceTokenProvider serviceTokenProvider) {
        this.restTemplate = restTemplate;
        this.interoperabilityServiceUrl = interoperabilityServiceUrl;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    private static final int MAX_ATTEMPTS = 3;

    public List<SourceDataResult> fetchScopedData(String citizenId, List<String> allowedScopes) {
        String url = interoperabilityServiceUrl + "/api/v1/interop/fetch/scoped";
        
        HttpHeaders headers = new HttpHeaders();
        // Dynamically fetch OAuth2 token via client_credentials
        headers.set("Authorization", serviceTokenProvider.getAuthorizationHeader());
        
        ScopedInteropRequest request = new ScopedInteropRequest(citizenId, allowedScopes);
        HttpEntity<ScopedInteropRequest> entity = new HttpEntity<>(request, headers);

        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            try {
                ResponseEntity<List<SourceDataResult>> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<List<SourceDataResult>>() {}
                );
                return response.getBody();
            } catch (org.springframework.web.client.HttpClientErrorException e) {
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
