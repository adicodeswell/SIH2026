package com.mahasetu.interoperability.connector;

import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Adapter for modern external government systems that communicate via REST/JSON.
 * For Phase 1, this connects to the Employment Mock System.
 */
@Component("EMPLOYMENT_SYSTEM_CONNECTOR")
public class RestConnector implements GovernmentSystemConnector {

    private final WebClient webClient;

    public RestConnector(WebClient.Builder webClientBuilder) {
        // In a real production app, this URL would come from application.yml
        // Hardcoded here to point exactly to our Mock System on port 8091
        this.webClient = webClientBuilder.baseUrl("http://mock-systems:8091").build();
    }

    @Override
    public RawExternalResponse fetch(String citizenId, ExternalSystem system) {
        
        // 1. Make the HTTP GET request to the mock system
        // e.g., http://mock-systems:8091/employment/MH1001
        String rawJsonString = webClient.get()
                .uri("/employment/" + citizenId)
                .retrieve()
                .bodyToMono(String.class) // Grab the raw JSON as a plain String
                .block(); // Wait for the response (blocking for MVP simplicity)
                
        // 2. Wrap it in our standard container and return it
        return new RawExternalResponse(rawJsonString, system);
    }
}
