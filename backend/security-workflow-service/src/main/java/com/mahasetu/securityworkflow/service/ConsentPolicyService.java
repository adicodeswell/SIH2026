package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.config.ConsentPolicyProperties;
import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import com.mahasetu.securityworkflow.dto.DataScope;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.exception.UnsupportedServiceCodeException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves consent policy (dataScope + purpose) for a given service code.
 * Validates configured scopes during startup and exposes strongly typed resolved policies.
 * Uses default-deny for unknown codes.
 */
@Service
public class ConsentPolicyService {

    private static final Logger log = LoggerFactory.getLogger(ConsentPolicyService.class);

    private final ConsentPolicyProperties consentPolicyProperties;
    private final Map<String, ResolvedConsentPolicy> resolvedPolicies = new ConcurrentHashMap<>();

    public ConsentPolicyService(ConsentPolicyProperties consentPolicyProperties) {
        this.consentPolicyProperties = consentPolicyProperties;
    }

    @PostConstruct
    public void validateAndInitializePolicies() {
        if (consentPolicyProperties.getPolicies() == null) {
            log.warn("No consent policies configured.");
            return;
        }

        for (Map.Entry<String, ConsentPolicy> entry : consentPolicyProperties.getPolicies().entrySet()) {
            String serviceCode = entry.getKey();
            ConsentPolicy config = entry.getValue();

            if (config.getRequestingDepartmentId() == null || config.getRequestingDepartmentId().trim().isEmpty()) {
                throw new IllegalStateException("Policy for " + serviceCode + " is missing requestingDepartmentId");
            }

            Set<DataScope> scopes = parseAndValidateScopes(config.getDataScope());

            resolvedPolicies.put(serviceCode, new ResolvedConsentPolicy(
                    serviceCode, scopes, config.getPurpose(), config.getDataScope(), config.getRequestingDepartmentId()
            ));

            log.info("Loaded and validated policy for {}: requiredScopes={}, purpose={}, dept={}", 
                    serviceCode, scopes, config.getPurpose(), config.getRequestingDepartmentId());
        }
    }

    private Set<DataScope> parseAndValidateScopes(String dataScopeStr) {
        if (dataScopeStr == null || dataScopeStr.trim().isEmpty()) {
            log.error("Data scope configuration cannot be empty or blank.");
            throw new IllegalStateException("Data scope configuration cannot be empty or blank.");
        }

        Set<DataScope> scopes = EnumSet.noneOf(DataScope.class);
        String[] parts = dataScopeStr.split(",");
        
        for (String part : parts) {
            String cleanPart = part.trim().toUpperCase();
            if (!cleanPart.isEmpty()) {
                try {
                    scopes.add(DataScope.valueOf(cleanPart));
                } catch (IllegalArgumentException e) {
                    log.error("Invalid data scope configured: '{}'. Allowed values are: {}", 
                            part, java.util.Arrays.toString(DataScope.values()));
                    throw new IllegalStateException("Invalid data scope configured: '" + part + "'");
                }
            }
        }
        
        if (scopes.isEmpty()) {
            throw new IllegalStateException("Data scope configuration must contain at least one valid scope.");
        }
        
        return scopes;
    }

    public ResolvedConsentPolicy getPolicy(String serviceCode) {
        if (serviceCode == null || serviceCode.trim().isEmpty()) {
            log.warn("Consent policy lookup called with null/empty serviceCode");
            throw new UnsupportedServiceCodeException(serviceCode);
        }

        ResolvedConsentPolicy policy = resolvedPolicies.get(serviceCode);

        if (policy == null) {
            log.warn("No consent policy found for serviceCode={} — default-deny applies", serviceCode);
            throw new UnsupportedServiceCodeException(serviceCode);
        }

        log.debug("Resolved policy for serviceCode={}: requiredScopes={}, purpose={}",
                serviceCode, policy.getRequiredScopes(), policy.getPurpose());

        return policy;
    }
}
