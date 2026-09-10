package com.mahasetu.securityworkflow.dto;

import java.util.Collections;
import java.util.Set;

public class ResolvedConsentPolicy {
    private final String serviceCode;
    private final Set<DataScope> requiredScopes;
    private final String purpose;
    private final String rawDataScope;
    private final String requestingDepartmentId;
    private final Set<String> requiredFields;
    private final Set<String> optionalFields;

    public ResolvedConsentPolicy(String serviceCode, Set<DataScope> requiredScopes, String purpose, String rawDataScope, String requestingDepartmentId, Set<String> requiredFields, Set<String> optionalFields) {
        this.serviceCode = serviceCode;
        this.requiredScopes = Collections.unmodifiableSet(requiredScopes);
        this.purpose = purpose;
        this.rawDataScope = rawDataScope;
        this.requestingDepartmentId = requestingDepartmentId;
        this.requiredFields = requiredFields != null ? Collections.unmodifiableSet(requiredFields) : Collections.emptySet();
        this.optionalFields = optionalFields != null ? Collections.unmodifiableSet(optionalFields) : Collections.emptySet();
    }

    public String getServiceCode() { return serviceCode; }
    public Set<DataScope> getRequiredScopes() { return requiredScopes; }
    public String getPurpose() { return purpose; }
    public String getRawDataScope() { return rawDataScope; }
    public String getRequestingDepartmentId() { return requestingDepartmentId; }
    public Set<String> getRequiredFields() { return requiredFields; }
    public Set<String> getOptionalFields() { return optionalFields; }
}
