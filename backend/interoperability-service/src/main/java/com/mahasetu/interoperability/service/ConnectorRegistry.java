package com.mahasetu.interoperability.service;

import com.mahasetu.interoperability.connector.GovernmentSystemConnector;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import com.mahasetu.interoperability.transformer.DataTransformer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ConnectorRegistry {

    private final Map<String, GovernmentSystemConnector> connectors;
    private final Map<String, DataTransformer> transformers;

    @Autowired
    public ConnectorRegistry(Map<String, GovernmentSystemConnector> connectors, 
                             Map<String, DataTransformer> transformers) {
        this.connectors = connectors;
        this.transformers = transformers;
    }

    @Cacheable(value = "citizenData", key = "#system.name() + '_' + #citizenId")
    @CircuitBreaker(name = "externalService", fallbackMethod = "fetchDataFallback")
    public CanonicalCitizenData fetchData(ExternalSystem system, String citizenId) {
        String systemName = system.name();
        
        GovernmentSystemConnector connector = connectors.get(systemName + "_CONNECTOR");
        if (connector == null) {
            throw new IllegalArgumentException("No connector found for system: " + systemName);
        }
        
        DataTransformer transformer = transformers.get(systemName + "_TRANSFORMER");
        if (transformer == null) {
            throw new IllegalArgumentException("No transformer found for system: " + systemName);
        }

        RawExternalResponse rawData = connector.fetch(citizenId, system);
        return transformer.transform(rawData);
    }

    public CanonicalCitizenData fetchDataFallback(ExternalSystem system, String citizenId, Throwable t) {
        CanonicalCitizenData fallbackData = new CanonicalCitizenData();
        fallbackData.setCitizenId(citizenId);
        fallbackData.setFullName("SERVICE_UNAVAILABLE - " + system.name());
        return fallbackData;
    }

    @Async
    public CompletableFuture<CanonicalCitizenData> fetchDataAsync(ExternalSystem system, String citizenId) {
        return CompletableFuture.completedFuture(fetchData(system, citizenId));
    }
}
