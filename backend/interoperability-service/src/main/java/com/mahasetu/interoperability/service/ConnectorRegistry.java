package com.mahasetu.interoperability.service;

import com.mahasetu.interoperability.connector.GovernmentSystemConnector;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import com.mahasetu.interoperability.transformer.DataTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    public CanonicalCitizenData fetchData(ExternalSystem system, String citizenId) {
        // Find the right connector and transformer for the system
        String systemName = system.name();
        
        GovernmentSystemConnector connector = connectors.get(systemName + "_REST_CONNECTOR");
        if (connector == null) {
            throw new IllegalArgumentException("No connector found for system: " + systemName);
        }
        
        DataTransformer transformer = transformers.get(systemName + "_TRANSFORMER");
        if (transformer == null) {
            throw new IllegalArgumentException("No transformer found for system: " + systemName);
        }

        // 1. Fetch raw data
        RawExternalResponse rawData = connector.fetch(citizenId, system);
        
        // 2. Transform to Canonical Model
        return transformer.transform(rawData);
    }
}
