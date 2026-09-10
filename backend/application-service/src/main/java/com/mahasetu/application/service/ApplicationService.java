package com.mahasetu.application.service;

import com.mahasetu.application.dto.*;
import com.mahasetu.application.entity.*;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.exception.ValidationException;
import com.mahasetu.application.integration.WorkflowClient;
import com.mahasetu.application.dto.CitizenApplicationActivityResponse;
import com.mahasetu.application.integration.WorkflowAuditClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;

import com.mahasetu.application.repository.*;
import org.slf4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final CitizenRepository citizenRepository;
    private final ServiceRepository serviceRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventRepository eventRepository;
    private final WorkflowClient workflowClient;
    private final WorkflowAuditClient workflowAuditClient;

    public ApplicationService(ApplicationRepository applicationRepository,
                              CitizenRepository citizenRepository,
                              ServiceRepository serviceRepository,
                              ApplicationEventRepository eventRepository,
                              WorkflowClient workflowClient,
                              WorkflowAuditClient workflowAuditClient) {
        this.applicationRepository = applicationRepository;
        this.citizenRepository = citizenRepository;
        this.serviceRepository = serviceRepository;
        this.objectMapper = new ObjectMapper();
        this.eventRepository = eventRepository;
        this.workflowClient = workflowClient;
        this.workflowAuditClient = workflowAuditClient;
    }

    public ApplicationResponse createApplication(CreateApplicationRequest request) {
        Citizen citizen = citizenRepository.findByCitizenId(request.getCitizenId())
                .orElseThrow(() -> new ValidationException("Citizen not found: " + request.getCitizenId()));

        com.mahasetu.application.entity.Service service = serviceRepository.findByServiceCode(request.getServiceCode())
                .orElseThrow(() -> new ValidationException("Service not found: " + request.getServiceCode()));

        if (!service.isActive()) {
            throw new ValidationException("Service is not active: " + request.getServiceCode());
        }

        Application application = new Application();
        application.setApplicationNumber(generateApplicationNumber());
        application.setCitizen(citizen);
        application.setService(service);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setSubmittedAt(LocalDateTime.now()); // Or null? The original sets it here. Let's keep it or set it on submit.

        Application savedApplication = applicationRepository.save(application);

        recordEvent(savedApplication, "APPLICATION_CREATED", null, ApplicationStatus.DRAFT, "Application created", request.getCitizenId());

        return mapToResponse(savedApplication);
    }

    @Transactional(readOnly = true)
        public java.util.List<com.mahasetu.application.dto.CitizenApplicationSummaryResponse> getApplicationsForCitizen(String citizenId) {
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
    }

        public List<CitizenApplicationActivityResponse> getApplicationActivity(String applicationNumber) {
        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new com.mahasetu.application.exception.ResourceNotFoundException("Application not found"));

        List<CitizenApplicationActivityResponse> activities = new ArrayList<>();

        // Add application events
        List<com.mahasetu.application.entity.ApplicationEvent> events = eventRepository.findByApplication_ApplicationNumberOrderByOccurredAtAsc(applicationNumber);
        for (com.mahasetu.application.entity.ApplicationEvent event : events) {
            CitizenApplicationActivityResponse dto = new CitizenApplicationActivityResponse();
            dto.setId(event.getId().toString());
            dto.setType(event.getEventType());
            dto.setStatus(event.getNewStatus() != null ? event.getNewStatus().name() : null);
            dto.setOccurredAt(event.getOccurredAt());
            
            // Normalize
            if ("APPLICATION_CREATED".equals(event.getEventType())) {
                dto.setCategory("APPLICATION");
                dto.setTitle("Application Created");
                dto.setDescription("Application draft was created.");
                dto.setActorType("CITIZEN");
            } else if ("APPLICATION_SUBMITTED".equals(event.getEventType())) {
                dto.setCategory("APPLICATION");
                dto.setTitle("Application Submitted");
                dto.setDescription("Application was successfully submitted.");
                dto.setActorType("CITIZEN");
            } else if ("WORKFLOW_STARTED".equals(event.getEventType())) {
                dto.setCategory("WORKFLOW");
                dto.setTitle("Verification Started");
                dto.setDescription("Automated verification process has started.");
                dto.setActorType("SYSTEM");
            } else if ("WORKFLOW_START_FAILED".equals(event.getEventType())) {
                dto.setCategory("WORKFLOW");
                dto.setTitle("Verification Failed");
                dto.setDescription("Application processing encountered an issue.");
                dto.setActorType("SYSTEM");
            } else {
                dto.setCategory("GENERAL");
                dto.setTitle(event.getEventType());
                dto.setDescription(event.getDescription());
                dto.setActorType("SYSTEM");
            }
            activities.add(dto);
        }

        // Add security workflow audit logs
        try {
            List<Map<String, Object>> auditLogs = workflowAuditClient.getApplicationAuditLogs(applicationNumber);
            if (auditLogs != null) {
                for (Map<String, Object> log : auditLogs) {
                    String action = (String) log.get("action");
                    String occurredAtStr = (String) log.get("occurredAt");
                    LocalDateTime occurredAt = null;
                    if (occurredAtStr != null) {
                        try {
                            occurredAt = LocalDateTime.parse(occurredAtStr.replace("Z", ""));
                        } catch(Exception ignored) {}
                    }
                    
                    if (action == null || occurredAt == null) continue;

                    CitizenApplicationActivityResponse dto = new CitizenApplicationActivityResponse();
                    dto.setId((String) log.get("id"));
                    dto.setType(action);
                    dto.setOccurredAt(occurredAt);
                    
                    if ("CONSENT_GRANTED".equals(action)) {
                        dto.setCategory("CONSENT");
                        dto.setTitle("Consent Granted");
                        dto.setDescription("Digital consent granted for verification.");
                        dto.setActorType("CITIZEN");
                    } else if ("CONSENT_REVOKED".equals(action)) {
                        dto.setCategory("CONSENT");
                        dto.setTitle("Consent Revoked");
                        dto.setDescription("Digital consent revoked.");
                        dto.setActorType("CITIZEN");
                    } else if ("OFFICER_REVIEW".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        String meta = (String) log.get("metadata");
                        if (meta != null && meta.contains("decision=APPROVE")) {
                            dto.setTitle("Application Approved");
                            dto.setDescription("Application approved by the reviewing officer.");
                            dto.setStatus("APPROVED");
                        } else if (meta != null && meta.contains("decision=REJECT")) {
                            dto.setTitle("Application Rejected");
                            dto.setDescription("Application rejected by the reviewing officer.");
                            dto.setStatus("REJECTED");
                        } else {
                            dto.setTitle("Officer Review");
                            dto.setDescription("Officer review completed.");
                        }
                        dto.setActorType("OFFICER");
                    } else if ("OFFICER_CLAIM".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        dto.setTitle("Officer Assigned");
                        dto.setDescription("Application review assigned to a government officer.");
                        dto.setActorType("OFFICER");
                    } else if ("OFFICER_UNCLAIM".equals(action)) {
                        dto.setCategory("OFFICER_REVIEW");
                        dto.setTitle("Officer Unassigned");
                        dto.setDescription("Application review was returned to the review queue.");
                        dto.setActorType("OFFICER");
                    } else {
                        // Skip raw internal events like WORKFLOW_STARTED from workflow DB to avoid duplicates
                        if ("WORKFLOW_STARTED".equals(action)) continue;
                        
                        dto.setCategory("WORKFLOW");
                        dto.setTitle(action);
                        dto.setDescription("System activity.");
                        dto.setActorType("SYSTEM");
                    }
                    activities.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch workflow audit logs for application {}", applicationNumber, e);
            // Optionally add a fake event indicating partial unavailability
            CitizenApplicationActivityResponse err = new CitizenApplicationActivityResponse();
            err.setId("error");
            err.setType("ACTIVITY_PARTIALLY_UNAVAILABLE");
            err.setCategory("SYSTEM");
            err.setTitle("Activity Partially Unavailable");
            err.setDescription("Some workflow activities could not be loaded at this time.");
            err.setOccurredAt(LocalDateTime.now());
            err.setActorType("SYSTEM");
            activities.add(err);
        }

        activities.sort(Comparator.comparing(CitizenApplicationActivityResponse::getOccurredAt));
        return activities;
    }

    public ApplicationResponse getApplication(String applicationNumber) {
        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationNumber));
        return mapToResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(String applicationNumber, UpdateApplicationStatusRequest request) {
        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationNumber));

        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus newStatus = request.getStatus();

        validateStateTransition(oldStatus, newStatus);

        application.setStatus(newStatus);

        Application updatedApplication = applicationRepository.save(application);

        recordEvent(updatedApplication, "STATUS_UPDATED", oldStatus, newStatus, request.getDescription(), request.getPerformedBy());

        return mapToResponse(updatedApplication);
    }

    @Transactional
    public ApplicationResponse submitApplication(String applicationNumber, String citizenId, com.mahasetu.application.integration.ConsentClient consentClient) {
        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationNumber));
                
        if (!application.getCitizen().getCitizenId().equals(citizenId)) {
             throw new org.springframework.security.access.AccessDeniedException("Cannot submit an application you do not own");
        }

        if (application.getStatus() != ApplicationStatus.DRAFT) {
             throw new ValidationException("Only DRAFT applications can be submitted");
        }

        boolean hasConsent = consentClient.hasConsentForApplication(applicationNumber);
        if (!hasConsent) {
             throw new ValidationException("Cannot submit application: Required consent is not granted");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());
        Application savedApplication = applicationRepository.save(application);

        recordEvent(savedApplication, "APPLICATION_SUBMITTED", oldStatus, ApplicationStatus.SUBMITTED, "Application submitted", citizenId);

        String workflowKey = application.getService().getWorkflowKey() != null && !application.getService().getWorkflowKey().isBlank()
                ? application.getService().getWorkflowKey()
                : "application-orchestration";

        try {
            workflowClient.startWorkflow(savedApplication.getApplicationNumber(), workflowKey);
            recordEvent(savedApplication, "WORKFLOW_STARTED", ApplicationStatus.SUBMITTED, ApplicationStatus.SUBMITTED,
                    "Workflow started: " + workflowKey, "application-service");
        } catch (Exception e) {
            log.error("Failed to start workflow", e);
            savedApplication.setStatus(ApplicationStatus.FAILED);
            savedApplication = applicationRepository.save(savedApplication);
            recordEvent(savedApplication, "WORKFLOW_START_FAILED", ApplicationStatus.SUBMITTED, ApplicationStatus.FAILED,
                    "Workflow start failed", "application-service");
            throw new RuntimeException("Workflow failed to start. Application submission aborted.");
        }

        return mapToResponse(savedApplication);
    }

    public ApplicationResponse applyWorkflowStatusCallback(String applicationNumber, WorkflowStatusCallbackRequest request) {
        if (request.getApplicationId() != null && !applicationNumber.equals(request.getApplicationId())) {
            throw new ValidationException("applicationId path and payload must match");
        }

        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationNumber));

        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus newStatus = mapWorkflowStatus(request.getStatus());

        if (oldStatus == newStatus) {
            return mapToResponse(application);
        }

        try {
            validateWorkflowTransition(oldStatus, newStatus);
        } catch (ValidationException e) {
            log.warn("Ignoring invalid or delayed workflow status transition from {} to {} for application {}", oldStatus, newStatus, applicationNumber);
            return mapToResponse(application);
        }

        application.setStatus(newStatus);

        if (request.getVerificationData() != null) {
            try {
                application.setVerificationData(objectMapper.writeValueAsString(request.getVerificationData()));
            } catch(Exception e) {
                log.error("Failed to serialize verification data", e);
            }
        }
        Application updatedApplication = applicationRepository.save(application);

        recordEvent(updatedApplication, workflowEventType(newStatus), oldStatus, newStatus,
                workflowEventDescription(request), workflowActor(request));

        return mapToResponse(updatedApplication);
    }

    @Transactional
    public ApplicationResponse retryWorkflow(String applicationNumber) {
        log.info("[RECOVERY_EVENT] Initiating workflow retry for applicationNumber={}", applicationNumber);
        Application application = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationNumber));

        if (application.getStatus() != ApplicationStatus.FAILED && application.getStatus() != ApplicationStatus.SUBMITTED) {
            log.warn("[RECOVERY_EVENT] Workflow retry rejected: applicationNumber={} is in status {}", applicationNumber, application.getStatus());
            throw new ValidationException("Cannot retry workflow for application in status: " + application.getStatus());
        }

        com.mahasetu.application.entity.Service service = application.getService();
        String workflowKey = service.getWorkflowKey() != null && !service.getWorkflowKey().isBlank()
                ? service.getWorkflowKey()
                : "application-orchestration";

        try {
            workflowClient.startWorkflow(application.getApplicationNumber(), workflowKey);
            ApplicationStatus oldStatus = application.getStatus();
            application.setStatus(ApplicationStatus.SUBMITTED);
            Application updated = applicationRepository.save(application);
            recordEvent(updated, "WORKFLOW_RETRY_SUCCEEDED", oldStatus, ApplicationStatus.SUBMITTED,
                    "Workflow start retry succeeded: " + workflowKey, "application-service");
            log.info("[RECOVERY_EVENT] Workflow retry succeeded for applicationNumber={}", applicationNumber);
            return mapToResponse(updated);
        } catch (Exception e) {
            log.error("[RECOVERY_EVENT] Workflow retry failed for applicationNumber={}: error={}", applicationNumber, e.getMessage());
            recordEvent(application, "WORKFLOW_RETRY_FAILED", application.getStatus(), ApplicationStatus.FAILED,
                    "Workflow start retry failed: " + e.getMessage(), "application-service");
            throw new RuntimeException("Workflow retry failed: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<TimelineEventResponse> getApplicationTimeline(String applicationNumber) {
        // Validate existence
        if (applicationRepository.findByApplicationNumber(applicationNumber).isEmpty()) {
            throw new ResourceNotFoundException("Application not found: " + applicationNumber);
        }

        List<ApplicationEvent> events = eventRepository.findByApplication_ApplicationNumberOrderByOccurredAtAsc(applicationNumber);
        return events.stream().map(event -> {
            TimelineEventResponse response = new TimelineEventResponse();
            response.setEventType(event.getEventType());
            response.setDescription(event.getDescription());
            response.setOccurredAt(event.getOccurredAt());
            return response;
        }).collect(Collectors.toList());
    }

    private void recordEvent(Application application, String eventType, ApplicationStatus oldStatus, ApplicationStatus newStatus, String description, String performedBy) {
        ApplicationEvent event = new ApplicationEvent();
        event.setApplication(application);
        event.setEventType(eventType);
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setDescription(description);
        event.setPerformedBy(performedBy);
        eventRepository.save(event);
    }

    private String generateApplicationNumber() {
        // Format: MH-YYYY-XXXXXX
        String year = String.valueOf(Year.now().getValue());
        // For MVP, using a short random UUID part. In production, use a sequence generator.
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "MH-" + year + "-" + randomPart;
    }

    private void validateStateTransition(ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        if (oldStatus == newStatus) return;

        boolean isValid = switch (oldStatus) {
            case DRAFT -> newStatus == ApplicationStatus.SUBMITTED;
            case SUBMITTED -> newStatus == ApplicationStatus.IN_PROGRESS || newStatus == ApplicationStatus.PENDING_VERIFICATION || newStatus == ApplicationStatus.PENDING_OFFICER_REVIEW || newStatus == ApplicationStatus.CONSENT_DENIED || newStatus == ApplicationStatus.FAILED || newStatus == ApplicationStatus.CANCELLED;
            case PENDING_VERIFICATION -> newStatus == ApplicationStatus.IN_PROGRESS || newStatus == ApplicationStatus.PENDING_REVIEW || newStatus == ApplicationStatus.PENDING_OFFICER_REVIEW || newStatus == ApplicationStatus.FAILED;
            case PENDING_REVIEW, PENDING_OFFICER_REVIEW -> newStatus == ApplicationStatus.APPROVED || newStatus == ApplicationStatus.REJECTED || newStatus == ApplicationStatus.FAILED;
            case APPROVED -> newStatus == ApplicationStatus.COMPLETED;
            case REJECTED, CONSENT_DENIED, FAILED, CANCELLED, COMPLETED -> false; // Terminal states
            case IN_PROGRESS -> newStatus == ApplicationStatus.PENDING_VERIFICATION || newStatus == ApplicationStatus.PENDING_REVIEW || newStatus == ApplicationStatus.PENDING_OFFICER_REVIEW || newStatus == ApplicationStatus.FAILED || newStatus == ApplicationStatus.CANCELLED;
        };

        if (!isValid) {
            throw new ValidationException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }
    }

    private boolean isTerminalState(ApplicationStatus status) {
        return status == ApplicationStatus.APPROVED ||
               status == ApplicationStatus.REJECTED ||
               status == ApplicationStatus.CONSENT_DENIED ||
               status == ApplicationStatus.FAILED ||
               status == ApplicationStatus.CANCELLED ||
               status == ApplicationStatus.COMPLETED;
    }

    private ApplicationStatus mapWorkflowStatus(String workflowStatus) {
        if (workflowStatus == null || workflowStatus.isBlank()) {
            throw new ValidationException("Workflow status must not be blank");
        }

        return switch (workflowStatus.trim().toUpperCase()) {
            case "PENDING_OFFICER_REVIEW" -> ApplicationStatus.PENDING_OFFICER_REVIEW;
            case "APPROVED" -> ApplicationStatus.APPROVED;
            case "REJECTED" -> ApplicationStatus.REJECTED;
            case "CONSENT_DENIED" -> ApplicationStatus.CONSENT_DENIED;
            case "FAILED" -> ApplicationStatus.FAILED;
            default -> throw new ValidationException("Unsupported workflow status: " + workflowStatus);
        };
    }

    private void validateWorkflowTransition(ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        validateStateTransition(oldStatus, newStatus);
    }

    private String workflowEventType(ApplicationStatus status) {
        return switch (status) {
            case PENDING_OFFICER_REVIEW -> "WORKFLOW_PENDING_REVIEW";
            case APPROVED -> "WORKFLOW_APPROVED";
            case REJECTED -> "WORKFLOW_REJECTED";
            case CONSENT_DENIED -> "WORKFLOW_CONSENT_DENIED";
            case FAILED -> "WORKFLOW_FAILED";
            default -> "WORKFLOW_STATUS_UPDATED";
        };
    }

    private String workflowEventDescription(WorkflowStatusCallbackRequest request) {
        if (request.getFailureReason() != null && !request.getFailureReason().isBlank()) {
            return request.getFailureReason();
        }
        return "Workflow status callback received: " + request.getStatus();
    }

    private String workflowActor(WorkflowStatusCallbackRequest request) {
        if (request.getOfficerId() != null && !request.getOfficerId().isBlank()) {
            return request.getOfficerId();
        }
        return "security-workflow-service";
    }

    private ApplicationResponse mapToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setApplicationNumber(application.getApplicationNumber());
        response.setStatus(application.getStatus());
        response.setCitizenId(application.getCitizen().getCitizenId());
        response.setServiceCode(application.getService().getServiceCode());
        response.setServiceName(application.getService().getServiceName());
        response.setDepartmentCode(application.getService().getDepartment().getDepartmentCode());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        response.setSubmittedAt(application.getSubmittedAt());
        if (application.getVerificationData() != null) {
            try {
                response.setVerificationData(objectMapper.readValue(application.getVerificationData(), Object.class));
            } catch (Exception e) {
                log.error("Failed to deserialize verification data", e);
            }
        }
        return response;
    }
}
