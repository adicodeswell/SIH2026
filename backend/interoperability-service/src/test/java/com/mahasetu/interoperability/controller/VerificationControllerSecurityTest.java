package com.mahasetu.interoperability.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.interoperability.dto.ScopedInteropRequest;
import com.mahasetu.interoperability.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.mahasetu.interoperability.service.ConnectorRegistry;
import com.mahasetu.interoperability.service.ScopeMappingService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import com.mahasetu.interoperability.model.DataScope;
import com.mahasetu.interoperability.model.ExternalSystem;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import com.mahasetu.interoperability.model.SourceDataResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VerificationController.class)
@Import(SecurityConfig.class)
public class VerificationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConnectorRegistry connectorRegistry;

    @MockBean
    private ScopeMappingService scopeMappingService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private Jwt createMockJwt(String clientId) {
        return new Jwt("mock-token", Instant.now(), Instant.now().plusSeconds(3600), 
                Map.of("alg", "none"), Map.of("azp", clientId));
    }

    @Test
    public void testFetchScopedData_AllowedWithTrustedService() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("security-workflow-service"));
        when(scopeMappingService.getSystemForScope(DataScope.EDUCATION)).thenReturn(Optional.of(ExternalSystem.EDUCATION_SYSTEM));
        when(connectorRegistry.fetchDataAsync(ExternalSystem.EDUCATION_SYSTEM, "CIT-123")).thenReturn(CompletableFuture.completedFuture(new SourceDataResult()));

        ScopedInteropRequest request = new ScopedInteropRequest();
        request.setCitizenId("CIT-123");
        request.setAllowedScopes(Arrays.asList("EDUCATION"));

        mockMvc.perform(post("/api/v1/interop/fetch/scoped")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testFetchScopedData_UnauthorizedWithoutToken() throws Exception {
        ScopedInteropRequest request = new ScopedInteropRequest();
        request.setCitizenId("CIT-123");
        request.setAllowedScopes(Arrays.asList("EDUCATION"));

        mockMvc.perform(post("/api/v1/interop/fetch/scoped")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testFetchScopedData_ForbiddenWithWrongClient() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("rogue-frontend-client"));

        ScopedInteropRequest request = new ScopedInteropRequest();
        request.setCitizenId("CIT-123");
        request.setAllowedScopes(Arrays.asList("EDUCATION"));

        mockMvc.perform(post("/api/v1/interop/fetch/scoped")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testFetchScopedData_ForbiddenWithDifferentService() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("application-service"));

        ScopedInteropRequest request = new ScopedInteropRequest();
        request.setCitizenId("CIT-123");
        request.setAllowedScopes(Arrays.asList("EDUCATION"));

        mockMvc.perform(post("/api/v1/interop/fetch/scoped")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
