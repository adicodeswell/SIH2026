package com.mahasetu.application.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateApplicationRequest {
    @NotBlank(message = "citizenId must not be blank")
    private String citizenId;

    @NotBlank(message = "serviceCode must not be blank")
    private String serviceCode;

    // Getters and Setters
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
}
