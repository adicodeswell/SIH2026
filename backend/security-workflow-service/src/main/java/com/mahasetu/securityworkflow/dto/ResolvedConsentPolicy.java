package com.mahasetu.securityworkflow.dto;

import java.util.Collections;
import java.util.Set;

public class ResolvedConsentPolicy {
    private final String serviceCode;
    private final Set<DataScope> requiredScopes;
    private final String purpose;
    private final String rawDataScope;
    private final String requestingDepartmentId;

    public ResolvedConsentPolicy(String serviceCode, Set<DataScope> requiredScopes, String purpose, String rawDataScope, String requestingDepartmentId) {
        this.serviceCode = serviceCode;
        this.requiredScopes = Collections.unmodifiableSet(requiredScopes);
        this.purpose = purpose;
        this.rawDataScope = rawDataScope;
        this.requestingDepartmentId = requestingDepartmentId;
    }

    public String getServiceCode() { return serviceCode; }
    public Set<DataScope> getRequiredScopes() { return requiredScopes; }
    public String getPurpose() { return purpose; }
    public String getRawDataScope() { return rawDataScope; }
    public String getRequestingDepartmentId() { return requestingDepartmentId; }
}
