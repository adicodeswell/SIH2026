package com.mahasetu.securityworkflow.dto;

public class ConsentPolicy {
    private String dataScope;
    private String purpose;
    private String requestingDepartmentId;

    public ConsentPolicy() {}

    public ConsentPolicy(String dataScope, String purpose, String requestingDepartmentId) {
        this.dataScope = dataScope;
        this.purpose = purpose;
        this.requestingDepartmentId = requestingDepartmentId;
    }

    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getRequestingDepartmentId() { return requestingDepartmentId; }
    public void setRequestingDepartmentId(String requestingDepartmentId) { this.requestingDepartmentId = requestingDepartmentId; }
}
