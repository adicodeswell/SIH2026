package com.mahasetu.securityworkflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
import com.mahasetu.securityworkflow.dto.verification.VerificationResult;
import com.mahasetu.securityworkflow.dto.verification.VerificationStatus;
import com.mahasetu.securityworkflow.service.worker.VerifyDataWorker;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VerifyDataWorkerTest {

    private VerifyDataWorker worker;
    private VerificationService verificationService;
    private ConsentPolicyService consentPolicyService;
    private ObjectMapper objectMapper;
    private DelegateExecution execution;

    @BeforeEach
    void setUp() {
        verificationService = mock(VerificationService.class);
        consentPolicyService = mock(ConsentPolicyService.class);
        objectMapper = new ObjectMapper();
        worker = new VerifyDataWorker(verificationService, consentPolicyService, objectMapper);
        execution = mock(DelegateExecution.class);
    }

    @Test
    void testExecute_ValidatesAndRemovesRawData() throws Exception {
        when(execution.getVariable("applicationId")).thenReturn("APP-123");
        when(execution.getVariable("serviceCode")).thenReturn("TEST");
        when(execution.getVariable("interoperabilityResult")).thenReturn("[{\"source\":\"TEST_SYSTEM\"}]");

        ResolvedConsentPolicy policy = new ResolvedConsentPolicy("TEST", Collections.emptySet(), "purpose", "test", "DEPT", Collections.emptySet(), Collections.emptySet());
        when(consentPolicyService.getPolicy("TEST")).thenReturn(policy);

        VerificationResult mockResult = new VerificationResult(VerificationStatus.VERIFIED, Collections.emptyList(), Collections.emptyList());
        when(verificationService.verify(any(), eq(policy))).thenReturn(mockResult);

        worker.execute(execution);

        verify(execution).removeVariable("interoperabilityResult");
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(execution).setVariable(eq("verificationResult"), captor.capture());
        
        VerificationResult actualResult = objectMapper.readValue(captor.getValue(), VerificationResult.class);
        assertEquals(VerificationStatus.VERIFIED, actualResult.getOverallStatus());
    }
}
