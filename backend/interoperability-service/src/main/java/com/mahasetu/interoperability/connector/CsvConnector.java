package com.mahasetu.interoperability.connector;

import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Adapter for external government systems that dump CSV files (simulated over HTTP).
 */
@Component("EDUCATION_SYSTEM_CONNECTOR")
public class CsvConnector implements GovernmentSystemConnector {

    private final WebClient webClient;

    public CsvConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8091").build();
    }

    @Override
    public RawExternalResponse fetch(String citizenId, ExternalSystem system) {
        String rawCsvString = webClient.get()
                .uri("/education/" + citizenId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        return new RawExternalResponse(rawCsvString, system);
    }
}
