package com.mahasetu.securityworkflow.dto;

import java.util.List;

public class ScopedInteropRequest {
    private String citizenId;
    private List<String> allowedScopes;
    
    public ScopedInteropRequest() {}
    public ScopedInteropRequest(String citizenId, List<String> allowedScopes) {
        this.citizenId = citizenId;
        this.allowedScopes = allowedScopes;
    }

    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public List<String> getAllowedScopes() { return allowedScopes; }
    public void setAllowedScopes(List<String> allowedScopes) { this.allowedScopes = allowedScopes; }
}
