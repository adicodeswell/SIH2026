package com.mahasetu.securityworkflow.service.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
import com.mahasetu.securityworkflow.dto.verification.VerificationResult;
import com.mahasetu.securityworkflow.service.ConsentPolicyService;
import com.mahasetu.securityworkflow.service.VerificationService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VerifyDataWorker implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(VerifyDataWorker.class);

    private final VerificationService verificationService;
    private final ConsentPolicyService consentPolicyService;
    private final ObjectMapper objectMapper;

    public VerifyDataWorker(VerificationService verificationService, ConsentPolicyService consentPolicyService, ObjectMapper objectMapper) {
        this.verificationService = verificationService;
        this.consentPolicyService = consentPolicyService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String applicationId = (String) execution.getVariable("applicationId");
        String serviceCode = (String) execution.getVariable("serviceCode");
        String rawDataJson = (String) execution.getVariable("interoperabilityResult");

        log.info("[VERIFICATION_EVENT] VerifyDataWorker: starting verification for applicationId={}, serviceCode={}", applicationId, serviceCode);

        ResolvedConsentPolicy policy = consentPolicyService.getPolicy(serviceCode);

        List<SourceDataResult> results;
        if (rawDataJson != null && !rawDataJson.isEmpty()) {
            results = objectMapper.readValue(rawDataJson, new TypeReference<List<SourceDataResult>>() {});
        } else {
            results = List.of();
        }

        VerificationResult verificationResult = verificationService.verify(results, policy);

        // Store only the minimized, safe verification result
        String verificationResultJson = objectMapper.writeValueAsString(verificationResult);
        execution.setVariable("verificationResult", verificationResultJson);
        
        // Ensure sensitive raw data is NOT propagated further down the workflow
        execution.removeVariable("interoperabilityResult");
        
        log.info("[VERIFICATION_EVENT] VerifyDataWorker: completed for applicationId={}, overallStatus={}", applicationId, verificationResult.getOverallStatus());
    }
}
