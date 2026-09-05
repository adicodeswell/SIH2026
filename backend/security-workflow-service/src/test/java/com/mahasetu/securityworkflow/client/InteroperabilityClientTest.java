package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InteroperabilityClientTest {

    @Mock
    private RestTemplate restTemplate;

    private InteroperabilityClient client;

    @BeforeEach
    void setUp() {
        client = new InteroperabilityClient(
                restTemplate,
                "http://localhost:8082",
                "test-token"
        );
    }

    @Test
    void testFetchAllData_SuccessOnFirstAttempt() {
        CanonicalCitizenData data = new CanonicalCitizenData();
        data.setCitizenId("CIT-123");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(data)));

        List<CanonicalCitizenData> result = client.fetchAllData("CIT-123");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CIT-123", result.get(0).getCitizenId());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void testFetchAllData_4xxClientError_FailsFastWithoutRetry() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertThrows(HttpClientErrorException.class, () -> client.fetchAllData("CIT-404"));

        // Must fail fast on attempt 1
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void testFetchAllData_5xxServerError_Retries3TimesThenThrows() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        assertThrows(HttpServerErrorException.class, () -> client.fetchAllData("CIT-500"));

        // Must retry up to 3 times
        verify(restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void testFetchAllData_ResourceAccessException_Retries3TimesThenThrows() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new ResourceAccessException("Connection refused"));

        assertThrows(ResourceAccessException.class, () -> client.fetchAllData("CIT-TIMEOUT"));

        // Must retry up to 3 times
        verify(restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void testFetchAllData_TransientError_SucceedsOnSecondAttempt() {
        CanonicalCitizenData data = new CanonicalCitizenData();
        data.setCitizenId("CIT-RETRY");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        ))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Unavailable"))
                .thenReturn(ResponseEntity.ok(List.of(data)));

        List<CanonicalCitizenData> result = client.fetchAllData("CIT-RETRY");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
}
