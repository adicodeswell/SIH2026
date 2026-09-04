package com.mahasetu.securityworkflow.dto;

import java.util.Date;

/**
 * Representation of a pending officer review task.
 * Contains metadata and process variables necessary for human decision making,
 * without exposing internal execution state or raw citizen secrets.
 */
public class OfficerReviewTaskResponse {

    private String taskId;
    private String taskName;
    private String applicationId;
    private String processInstanceId;
    private String citizenId;
    private String serviceCode;
    private Date createTime;
    private String candidateGroup;
    private String assignee;
    private String status;

    public OfficerReviewTaskResponse() {
    }

    public OfficerReviewTaskResponse(String taskId, String taskName, String applicationId,
                                     String processInstanceId, String citizenId, String serviceCode,
                                     Date createTime, String candidateGroup, String assignee, String status) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.applicationId = applicationId;
        this.processInstanceId = processInstanceId;
        this.citizenId = citizenId;
        this.serviceCode = serviceCode;
        this.createTime = createTime;
        this.candidateGroup = candidateGroup;
        this.assignee = assignee;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCandidateGroup() {
        return candidateGroup;
    }

    public void setCandidateGroup(String candidateGroup) {
        this.candidateGroup = candidateGroup;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
