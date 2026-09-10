with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'r') as f:
    content = f.read()

import re

new_method = """    public java.util.List<com.mahasetu.application.dto.CitizenApplicationSummaryResponse> getApplicationsForCitizen(String citizenId) {
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
    }

    public ApplicationResponse getApplication(String"""

content = content.replace("public ApplicationResponse getApplication(String", new_method)

with open('./backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java', 'w') as f:
    f.write(content)
