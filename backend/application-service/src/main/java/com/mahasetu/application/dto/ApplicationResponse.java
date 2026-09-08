package com.mahasetu.application.dto;

import com.mahasetu.application.entity.ApplicationStatus;
import java.time.LocalDateTime;

public class ApplicationResponse {
    private String applicationNumber;
    private ApplicationStatus status;
    private String citizenId;
    private String serviceCode;
    private LocalDateTime submittedAt;
    private Object verificationData;

    public Object getVerificationData() { return verificationData; }
    public void setVerificationData(Object verificationData) { this.verificationData = verificationData; }

    // Getters and Setters
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
