package com.mahasetu.securityworkflow.dto;

public class ConsentRequest {
    private String applicationId;
    private String serviceCode;
    
    // Kept to avoid Jackson deserialization failures from old frontends, but ignored by backend.
    private String dataScope;
    private String purpose;
    private String requestingDepartmentId;

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getRequestingDepartmentId() { return requestingDepartmentId; }
    public void setRequestingDepartmentId(String requestingDepartmentId) { this.requestingDepartmentId = requestingDepartmentId; }
}
