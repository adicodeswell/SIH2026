package com.mahasetu.application.dto;

import jakarta.validation.constraints.NotBlank;

public class WorkflowStatusCallbackRequest {

    @NotBlank(message = "applicationId must not be blank")
    private String applicationId;

    @NotBlank(message = "processInstanceId must not be blank")
    private String processInstanceId;

    @NotBlank(message = "status must not be blank")
    private String status;

    private String failureReason;
    private String officerId;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }
}
