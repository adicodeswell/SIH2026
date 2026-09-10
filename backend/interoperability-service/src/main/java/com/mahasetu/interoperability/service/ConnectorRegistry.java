package com.mahasetu.interoperability.service;

import com.mahasetu.interoperability.connector.GovernmentSystemConnector;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import com.mahasetu.interoperability.model.SourceDataResult;
import com.mahasetu.interoperability.transformer.DataTransformer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ConnectorRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConnectorRegistry.class);

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
    public SourceDataResult fetchData(ExternalSystem system, String citizenId) {
        String systemName = system.name();
        
        GovernmentSystemConnector connector = connectors.get(systemName + "_CONNECTOR");
        if (connector == null) {
            log.warn("No connector found for system: {}", systemName);
            return new SourceDataResult(systemName, "FAILED", "NO_CONNECTOR", null);
        }
        
        DataTransformer transformer = transformers.get(systemName + "_TRANSFORMER");
        if (transformer == null) {
            log.warn("No transformer found for system: {}", systemName);
            return new SourceDataResult(systemName, "FAILED", "NO_TRANSFORMER", null);
        }

        try {
            RawExternalResponse rawData = connector.fetch(citizenId, system);
            CanonicalCitizenData data = transformer.transform(rawData);
            return new SourceDataResult(systemName, "SUCCESS", null, data);
        } catch (Exception e) {
            log.error("Failed to fetch data from system: {}", systemName, e);
            throw e; // rethrow to trigger circuit breaker fallback
        }
    }

    public SourceDataResult fetchDataFallback(ExternalSystem system, String citizenId, Throwable t) {
        log.warn("Fallback triggered for system {} due to error: {}", system.name(), t.getMessage());
        return new SourceDataResult(system.name(), "FAILED", "SERVICE_UNAVAILABLE", null);
    }

    @Async
    public CompletableFuture<SourceDataResult> fetchDataAsync(ExternalSystem system, String citizenId) {
        return CompletableFuture.completedFuture(fetchData(system, citizenId));
    }
}
