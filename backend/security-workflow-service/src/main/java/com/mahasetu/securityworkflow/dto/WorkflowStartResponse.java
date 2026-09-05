package com.mahasetu.securityworkflow.dto;

public class WorkflowStartResponse {
    private String processInstanceId;

    public WorkflowStartResponse() {}

    public WorkflowStartResponse(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
