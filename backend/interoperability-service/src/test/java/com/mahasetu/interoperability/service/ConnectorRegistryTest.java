package com.mahasetu.interoperability.service;

import com.mahasetu.interoperability.connector.GovernmentSystemConnector;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import com.mahasetu.interoperability.transformer.DataTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ConnectorRegistryTest {

    private ConnectorRegistry registry;
    private GovernmentSystemConnector mockConnector;
    private DataTransformer mockTransformer;

    @BeforeEach
    public void setup() {
        mockConnector = Mockito.mock(GovernmentSystemConnector.class);
        mockTransformer = Mockito.mock(DataTransformer.class);
        
        Map<String, GovernmentSystemConnector> connectors = new HashMap<>();
        connectors.put("EMPLOYMENT_SYSTEM_CONNECTOR", mockConnector);
        
        Map<String, DataTransformer> transformers = new HashMap<>();
        transformers.put("EMPLOYMENT_SYSTEM_TRANSFORMER", mockTransformer);
        
        registry = new ConnectorRegistry(connectors, transformers);
    }

    @Test
    public void testFetchDataSuccessfully() {
        RawExternalResponse mockRaw = new RawExternalResponse("{}", ExternalSystem.EMPLOYMENT_SYSTEM);
        CanonicalCitizenData mockCleanData = new CanonicalCitizenData();
        mockCleanData.setCitizenId("MH1001");
        
        when(mockConnector.fetch("MH1001", ExternalSystem.EMPLOYMENT_SYSTEM)).thenReturn(mockRaw);
        when(mockTransformer.transform(mockRaw)).thenReturn(mockCleanData);
        
        CanonicalCitizenData result = registry.fetchData(ExternalSystem.EMPLOYMENT_SYSTEM, "MH1001");
        
        assertEquals("MH1001", result.getCitizenId());
    }

    @Test
    public void testFetchDataThrowsExceptionWhenConnectorMissing() {
        assertThrows(IllegalArgumentException.class, () -> {
            registry.fetchData(ExternalSystem.EDUCATION_SYSTEM, "MH1001");
        });
    }
}
