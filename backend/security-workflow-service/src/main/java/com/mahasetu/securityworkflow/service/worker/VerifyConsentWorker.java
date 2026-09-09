package com.mahasetu.securityworkflow.service.worker;

import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.exception.UnsupportedServiceCodeException;
import com.mahasetu.securityworkflow.service.ConsentPolicyService;
import com.mahasetu.securityworkflow.service.ConsentService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VerifyConsentWorker implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(VerifyConsentWorker.class);

    private final ConsentService consentService;
    private final ConsentPolicyService consentPolicyService;

    public VerifyConsentWorker(ConsentService consentService, ConsentPolicyService consentPolicyService) {
        this.consentService = consentService;
        this.consentPolicyService = consentPolicyService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String citizenId = (String) execution.getVariable("citizenId");
        String serviceCode = (String) execution.getVariable("serviceCode");
        String applicationId = (String) execution.getVariable("applicationId");

        log.info("[CONSENT_EVENT] VerifyConsentWorker: checking consent for applicationId={}, citizenId={}, serviceCode={}",
                applicationId, citizenId, serviceCode);

        // Dynamically resolve strongly typed consent policy from configuration
        ResolvedConsentPolicy policy;
        try {
            policy = consentPolicyService.getPolicy(serviceCode);
        } catch (UnsupportedServiceCodeException e) {
            log.error("[CONSENT_EVENT] VerifyConsentWorker: unsupported serviceCode={} for applicationId={}", serviceCode, applicationId);
            execution.setVariable("failureReason", "Unsupported service code: " + serviceCode);
            throw new BpmnError("UNSUPPORTED_SERVICE_CODE", "No consent policy for service code: " + serviceCode);
        }

        // Phase 2 secure, application-aware validation checking coverage, expiry, purpose and department
        boolean consentValid = consentService.checkConsentContext(citizenId, applicationId, serviceCode, policy);

        execution.setVariable("consentValid", consentValid);

        log.info("[CONSENT_EVENT] VerifyConsentWorker: consent check result for applicationId={}: consentValid={}",
                applicationId, consentValid);
    }
}
