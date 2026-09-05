package com.mahasetu.securityworkflow.dto;

import java.time.LocalDateTime;

/**
 * Confirmation response returned when an officer completes a review decision.
 */
public class OfficerDecisionResponse {

    private String taskId;
    private String applicationId;
    private String decision;
    private String officerId;
    private String reason;
    private LocalDateTime timestamp;
    private String status;

    public OfficerDecisionResponse() {
    }

    public OfficerDecisionResponse(String taskId, String applicationId, String decision,
                                   String officerId, String reason, LocalDateTime timestamp, String status) {
        this.taskId = taskId;
        this.applicationId = applicationId;
        this.decision = decision;
        this.officerId = officerId;
        this.reason = reason;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
