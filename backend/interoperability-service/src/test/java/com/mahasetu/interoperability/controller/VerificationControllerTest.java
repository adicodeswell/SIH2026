package com.mahasetu.interoperability.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.interoperability.dto.ScopedInteropRequest;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.SourceDataResult;
import com.mahasetu.interoperability.service.ConnectorRegistry;
import com.mahasetu.interoperability.service.ScopeMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VerificationControllerTest {

    private MockMvc mockMvc;
    private ConnectorRegistry connectorRegistry;
    private ScopeMappingService scopeMappingService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        connectorRegistry = Mockito.mock(ConnectorRegistry.class);
        scopeMappingService = new ScopeMappingService();
        VerificationController controller = new VerificationController(connectorRegistry, scopeMappingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testFetchScopedData_EducationOnly() throws Exception {
        ScopedInteropRequest request = new ScopedInteropRequest();
        request.setCitizenId("CIT-123");
        request.setAllowedScopes(Arrays.asList("EDUCATION"));

        CanonicalCitizenData mockEduData = new CanonicalCitizenData();
        mockEduData.setHighestDegree("B.Tech");

        when(connectorRegistry.fetchDataAsync(eq(ExternalSystem.EDUCATION_SYSTEM), eq("CIT-123")))
                .thenReturn(CompletableFuture.completedFuture(
                        new SourceDataResult("EDUCATION_SYSTEM", "SUCCESS", null, mockEduData)
                ));

        mockMvc.perform(post("/api/v1/interop/fetch/scoped")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].source").value("EDUCATION_SYSTEM"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].data.highestDegree").value("B.Tech"));

        verify(connectorRegistry, times(1)).fetchDataAsync(eq(ExternalSystem.EDUCATION_SYSTEM), eq("CIT-123"));
        verify(connectorRegistry, never()).fetchDataAsync(eq(ExternalSystem.EMPLOYMENT_SYSTEM), anyString());
        verify(connectorRegistry, never()).fetchDataAsync(eq(ExternalSystem.HEALTH_SYSTEM), anyString());
        verify(connectorRegistry, never()).fetchDataAsync(eq(ExternalSystem.SKILLS_SYSTEM), anyString());
    }

    @Test
    public void testFetchScopedData_MultipleScopes() throws Exception {
        ScopedInteropRequest request = new ScopedInteropRequest();
        request.setCitizenId("CIT-123");
        request.setAllowedScopes(Arrays.asList("EDUCATION", "EMPLOYMENT", "SKILLS"));

        when(connectorRegistry.fetchDataAsync(eq(ExternalSystem.EDUCATION_SYSTEM), eq("CIT-123")))
                .thenReturn(CompletableFuture.completedFuture(new SourceDataResult("EDUCATION_SYSTEM", "SUCCESS", null, new CanonicalCitizenData())));
        when(connectorRegistry.fetchDataAsync(eq(ExternalSystem.EMPLOYMENT_SYSTEM), eq("CIT-123")))
                .thenReturn(CompletableFuture.completedFuture(new SourceDataResult("EMPLOYMENT_SYSTEM", "SUCCESS", null, new CanonicalCitizenData())));
        when(connectorRegistry.fetchDataAsync(eq(ExternalSystem.SKILLS_SYSTEM), eq("CIT-123")))
                .thenReturn(CompletableFuture.completedFuture(new SourceDataResult("SKILLS_SYSTEM", "SUCCESS", null, new CanonicalCitizenData())));

        mockMvc.perform(post("/api/v1/interop/fetch/scoped")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(connectorRegistry, times(1)).fetchDataAsync(eq(ExternalSystem.EDUCATION_SYSTEM), eq("CIT-123"));
        verify(connectorRegistry, times(1)).fetchDataAsync(eq(ExternalSystem.EMPLOYMENT_SYSTEM), eq("CIT-123"));
        verify(connectorRegistry, times(1)).fetchDataAsync(eq(ExternalSystem.SKILLS_SYSTEM), eq("CIT-123"));
        verify(connectorRegistry, never()).fetchDataAsync(eq(ExternalSystem.HEALTH_SYSTEM), anyString());
    }

    @Test
    public void testFetchScopedData_HealthOnly() throws Exception {
        ScopedInteropRequest request = new ScopedInteropRequest();
        request.setCitizenId("CIT-123");
        request.setAllowedScopes(Arrays.asList("HEALTH"));

        when(connectorRegistry.fetchDataAsync(eq(ExternalSystem.HEALTH_SYSTEM), eq("CIT-123")))
                .thenReturn(CompletableFuture.completedFuture(new SourceDataResult("HEALTH_SYSTEM", "SUCCESS", null, new CanonicalCitizenData())));

        mockMvc.perform(post("/api/v1/interop/fetch/scoped")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(connectorRegistry, times(1)).fetchDataAsync(eq(ExternalSystem.HEALTH_SYSTEM), eq("CIT-123"));
    }
}
