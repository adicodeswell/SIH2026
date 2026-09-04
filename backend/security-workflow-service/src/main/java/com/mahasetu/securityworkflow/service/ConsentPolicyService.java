package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.config.ConsentPolicyProperties;
import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import com.mahasetu.securityworkflow.exception.UnsupportedServiceCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Resolves consent policy (dataScope + purpose) for a given service code.
 * Uses configuration-driven policy lookup with default-deny for unknown codes.
 */
@Service
public class ConsentPolicyService {

    private static final Logger log = LoggerFactory.getLogger(ConsentPolicyService.class);

    private final ConsentPolicyProperties consentPolicyProperties;

    public ConsentPolicyService(ConsentPolicyProperties consentPolicyProperties) {
        this.consentPolicyProperties = consentPolicyProperties;
    }

    /**
     * Returns the consent policy for the given service code.
     *
     * @param serviceCode the service code from the application
     * @return the ConsentPolicy containing dataScope and purpose
     * @throws UnsupportedServiceCodeException if no policy is configured (default-deny)
     */
    public ConsentPolicy getPolicy(String serviceCode) {
        if (serviceCode == null || serviceCode.trim().isEmpty()) {
            log.warn("Consent policy lookup called with null/empty serviceCode");
            throw new UnsupportedServiceCodeException(serviceCode);
        }

        ConsentPolicy policy = consentPolicyProperties.getPolicies().get(serviceCode);

        if (policy == null) {
            log.warn("No consent policy found for serviceCode={} — default-deny applies", serviceCode);
            throw new UnsupportedServiceCodeException(serviceCode);
        }

        log.debug("Resolved consent policy for serviceCode={}: dataScope={}, purpose={}",
                serviceCode, policy.getDataScope(), policy.getPurpose());

        return policy;
    }
}
