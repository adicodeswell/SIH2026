package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.SourceDataResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class InteroperabilityClientTest {

    private RestTemplate restTemplate;
    private InteroperabilityClient client;

    @BeforeEach
    public void setup() {
        restTemplate = mock(RestTemplate.class);
        ServiceTokenProvider serviceTokenProvider = mock(ServiceTokenProvider.class);
        when(serviceTokenProvider.getAuthorizationHeader()).thenReturn("Bearer mock-token");
        client = new InteroperabilityClient(restTemplate, "http://localhost", serviceTokenProvider);
    }

    @Test
    public void testFetchScopedData_Success() {
        SourceDataResult mockData = new SourceDataResult();
        mockData.setSource("EDUCATION_SYSTEM");
        mockData.setStatus("SUCCESS");

        ResponseEntity<List<SourceDataResult>> responseEntity = new ResponseEntity<>(Collections.singletonList(mockData), HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<SourceDataResult> result = client.fetchScopedData("CIT-123", Arrays.asList("EDUCATION"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EDUCATION_SYSTEM", result.get(0).getSource());

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void testFetchScopedData_ClientError_NoRetry() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> client.fetchScopedData("CIT-123", Arrays.asList("EDUCATION")));

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void testFetchScopedData_ServerError_Retries() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> client.fetchScopedData("CIT-123", Arrays.asList("EDUCATION")));

        verify(restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void testFetchScopedData_ConnectionError_Retries() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new ResourceAccessException("Connection refused"));

        assertThrows(ResourceAccessException.class, () -> client.fetchScopedData("CIT-123", Arrays.asList("EDUCATION")));

        verify(restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
}
