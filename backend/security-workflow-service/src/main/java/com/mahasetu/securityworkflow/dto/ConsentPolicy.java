package com.mahasetu.securityworkflow.dto;

public class ConsentPolicy {
    private String dataScope;
    private String purpose;
    private String requestingDepartmentId;
    private String requiredFields;
    private String optionalFields;

    public ConsentPolicy() {}

    public ConsentPolicy(String dataScope, String purpose, String requestingDepartmentId, String requiredFields, String optionalFields) {
        this.dataScope = dataScope;
        this.purpose = purpose;
        this.requestingDepartmentId = requestingDepartmentId;
        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;
    }

    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getRequestingDepartmentId() { return requestingDepartmentId; }
    public void setRequestingDepartmentId(String requestingDepartmentId) { this.requestingDepartmentId = requestingDepartmentId; }

    public String getRequiredFields() { return requiredFields; }
    public void setRequiredFields(String requiredFields) { this.requiredFields = requiredFields; }

    public String getOptionalFields() { return optionalFields; }
    public void setOptionalFields(String optionalFields) { this.optionalFields = optionalFields; }
}
