package com.mahasetu.securityworkflow.client;

import com.mahasetu.securityworkflow.dto.WorkflowStatusCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowStatusClientTest {

    @Mock
    private RestTemplate restTemplate;

    private WorkflowStatusClient client;
    private static final String APP_SERVICE_URL = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        client = new WorkflowStatusClient(restTemplate, APP_SERVICE_URL);
    }

    @Test
    void testSendStatusCallback_SuccessPayload_SendsPostRequest() {
        WorkflowStatusCallback callback = new WorkflowStatusCallback(
                "APP-1001",
                "proc-inst-123",
                "SUCCESS",
                null
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(null);

        assertDoesNotThrow(() -> client.sendStatusCallback(callback));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(Void.class));

        assertEquals("http://localhost:8081/internal/v1/applications/APP-1001/workflow-status", urlCaptor.getValue());
        HttpEntity<WorkflowStatusCallback> sentEntity = entityCaptor.getValue();
        assertEquals(MediaType.APPLICATION_JSON, sentEntity.getHeaders().getContentType());
        assertEquals("SUCCESS", sentEntity.getBody().getStatus());
        assertEquals("proc-inst-123", sentEntity.getBody().getProcessInstanceId());
        assertNull(sentEntity.getBody().getFailureReason());
    }

    @Test
    void testSendStatusCallback_FailedPayload_SendsFailureReason() {
        WorkflowStatusCallback callback = new WorkflowStatusCallback(
                "APP-2002",
                "proc-inst-456",
                "FAILED",
                "Failed to fetch data from Interoperability Service"
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(null);

        assertDoesNotThrow(() -> client.sendStatusCallback(callback));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(Void.class));

        assertEquals("http://localhost:8081/internal/v1/applications/APP-2002/workflow-status", urlCaptor.getValue());
        HttpEntity<WorkflowStatusCallback> sentEntity = entityCaptor.getValue();
        assertEquals("FAILED", sentEntity.getBody().getStatus());
        assertEquals("Failed to fetch data from Interoperability Service", sentEntity.getBody().getFailureReason());
    }

    @Test
    void testSendStatusCallback_OnHttpFailure_DoesNotThrowException() {
        WorkflowStatusCallback callback = new WorkflowStatusCallback(
                "APP-3003",
                "proc-inst-789",
                "CONSENT_DENIED",
                null
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Must not throw — swallowing callback failures avoids recursive workflow crash
        assertDoesNotThrow(() -> client.sendStatusCallback(callback));
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Void.class));
    }
}
