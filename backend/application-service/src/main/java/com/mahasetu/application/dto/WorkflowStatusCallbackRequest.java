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
    private Object verificationData;
    private String officerDecision;
    private String officerDecisionReason;

    public String getOfficerDecision() { return officerDecision; }
    public void setOfficerDecision(String officerDecision) { this.officerDecision = officerDecision; }

    public String getOfficerDecisionReason() { return officerDecisionReason; }
    public void setOfficerDecisionReason(String officerDecisionReason) { this.officerDecisionReason = officerDecisionReason; }


    public Object getVerificationData() { return verificationData; }
    public void setVerificationData(Object verificationData) { this.verificationData = verificationData; }

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
