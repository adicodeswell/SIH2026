with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'r') as f:
    content = f.read()

import re

old_method = """    public java.util.List<com.mahasetu.application.dto.CitizenApplicationSummaryResponse> getApplicationsForCitizen(String citizenId) {
        return applicationRepository.findByCitizen_CitizenIdOrderByCreatedAtDesc(citizenId).stream()
                .map(app -> com.mahasetu.application.dto.CitizenApplicationSummaryResponse.builder()
                        .applicationNumber(app.getApplicationNumber())
                        .serviceCode(app.getService().getServiceCode())
                        .serviceName(app.getService().getServiceName())
                        .departmentCode(app.getService().getDepartmentId())
                        .status(app.getStatus())
                        .createdAt(app.getCreatedAt())
                        .submittedAt(app.getSubmittedAt())
                        .updatedAt(app.getUpdatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }"""

new_method = """    public java.util.List<com.mahasetu.application.dto.CitizenApplicationSummaryResponse> getApplicationsForCitizen(String citizenId) {
        return applicationRepository.findByCitizen_CitizenIdOrderByCreatedAtDesc(citizenId).stream()
                .map(app -> {
                    com.mahasetu.application.dto.CitizenApplicationSummaryResponse response = new com.mahasetu.application.dto.CitizenApplicationSummaryResponse();
                    response.setApplicationNumber(app.getApplicationNumber());
                    response.setServiceCode(app.getService().getServiceCode());
                    response.setServiceName(app.getService().getServiceName());
                    response.setDepartmentCode(app.getService().getDepartment().getDepartmentCode());
                    response.setDepartmentName(app.getService().getDepartment().getName());
                    response.setStatus(app.getStatus());
                    response.setCreatedAt(app.getCreatedAt());
                    response.setSubmittedAt(app.getSubmittedAt());
                    response.setUpdatedAt(app.getUpdatedAt());
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
    }"""

content = content.replace(old_method, new_method)

with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'w') as f:
    f.write(content)
