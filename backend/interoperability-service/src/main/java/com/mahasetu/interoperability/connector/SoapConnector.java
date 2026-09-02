package com.mahasetu.interoperability.connector;

import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Adapter for legacy government systems that communicate via SOAP/XML.
 */
@Component("HEALTH_SYSTEM_CONNECTOR")
public class SoapConnector implements GovernmentSystemConnector {

    private final WebClient webClient;

    public SoapConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8091").build();
    }

    @Override
    public RawExternalResponse fetch(String citizenId, ExternalSystem system) {
        String rawXmlString = webClient.get()
                .uri("/health/" + citizenId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        return new RawExternalResponse(rawXmlString, system);
    }
}
