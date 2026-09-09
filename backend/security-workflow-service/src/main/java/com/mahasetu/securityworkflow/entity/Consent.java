package com.mahasetu.securityworkflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "citizen_id", nullable = false)
    private String citizenId;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "service_code")
    private String serviceCode;

    @Column(name = "requesting_department_id", nullable = false)
    private String requestingDepartmentId;

    @Column(name = "data_scope", nullable = false)
    private String dataScope;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private String status;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getRequestingDepartmentId() { return requestingDepartmentId; }
    public void setRequestingDepartmentId(String requestingDepartmentId) { this.requestingDepartmentId = requestingDepartmentId; }

    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public java.util.Set<com.mahasetu.securityworkflow.dto.DataScope> getGrantedScopes() {
        if (dataScope == null || dataScope.trim().isEmpty()) return java.util.Collections.emptySet();
        java.util.Set<com.mahasetu.securityworkflow.dto.DataScope> scopes = java.util.EnumSet.noneOf(com.mahasetu.securityworkflow.dto.DataScope.class);
        for (String part : dataScope.split(",")) {
            String clean = part.trim().toUpperCase();
            if (!clean.isEmpty()) {
                try {
                    scopes.add(com.mahasetu.securityworkflow.dto.DataScope.valueOf(clean));
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Invalid persisted data scope: " + clean);
                }
            }
        }
        return scopes;
    }
}
