package com.mahasetu.securityworkflow.dto;

/**
 * DTO for workflow status callback to Application Service.
 * Contains only safe, correlatable information — no tokens, secrets, or PII.
 */
public class WorkflowStatusCallback {

    private String applicationId;
    private String processInstanceId;
    private String status;
    private String failureReason;

    public WorkflowStatusCallback() {
    }

    public WorkflowStatusCallback(String applicationId, String processInstanceId, String status, String failureReason) {
        this.applicationId = applicationId;
        this.processInstanceId = processInstanceId;
        this.status = status;
        this.failureReason = failureReason;
    }

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
}
