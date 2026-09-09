package com.mahasetu.securityworkflow.dto;

import java.util.Collections;
import java.util.Set;

/**
 * Clean, safe policy representation for a given service code.
 * Exposes strongly typed DataScopes and purpose.
 */
public class ResolvedConsentPolicy {
    private final String serviceCode;
    private final Set<DataScope> requiredScopes;
    private final String purpose;
    private final String rawDataScope;

    public ResolvedConsentPolicy(String serviceCode, Set<DataScope> requiredScopes, String purpose, String rawDataScope) {
        this.serviceCode = serviceCode;
        this.requiredScopes = Collections.unmodifiableSet(requiredScopes);
        this.purpose = purpose;
        this.rawDataScope = rawDataScope;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public Set<DataScope> getRequiredScopes() {
        return requiredScopes;
    }

    public String getPurpose() {
        return purpose;
    }

    /**
     * Preserved temporarily for backward compatibility with Phase 1 consent checks.
     */
    public String getRawDataScope() {
        return rawDataScope;
    }
}
