package com.mahasetu.application.dto;

import com.mahasetu.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateApplicationStatusRequest {
    @NotNull(message = "status must not be null")
    private ApplicationStatus status;

    private String description;
    private String performedBy;

    // Getters and Setters
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
