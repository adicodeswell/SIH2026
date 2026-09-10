with open('./backend/application-service/src/main/java/com/mahasetu/application/dto/ApplicationResponse.java', 'r') as f:
    content = f.read()

new_fields = """    private String serviceName;
    private String departmentCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
"""

content = content.replace("    public Object getVerificationData()", new_fields + "\n    public Object getVerificationData()")

with open('./backend/application-service/src/main/java/com/mahasetu/application/dto/ApplicationResponse.java', 'w') as f:
    f.write(content)

with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'r') as f:
    app_service_content = f.read()

mapping_patch = """        response.setServiceCode(application.getService().getServiceCode());
        response.setServiceName(application.getService().getServiceName());
        response.setDepartmentCode(application.getService().getDepartment().getDepartmentCode());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());"""

app_service_content = app_service_content.replace("        response.setServiceCode(application.getService().getServiceCode());", mapping_patch)

with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'w') as f:
    f.write(app_service_content)

