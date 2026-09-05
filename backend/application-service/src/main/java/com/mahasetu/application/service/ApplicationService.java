package com.mahasetu.application.service;

import com.mahasetu.application.dto.*;
import com.mahasetu.application.entity.*;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.exception.ValidationException;
import com.mahasetu.application.integration.WorkflowClient;
import com.mahasetu.application.repository.*;
import org.slf4j.Logger;
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
    private final ApplicationEventRepository eventRepository;
    private final WorkflowClient workflowClient;

    public ApplicationService(ApplicationRepository applicationRepository,
                              CitizenRepository citizenRepository,
                              ServiceRepository serviceRepository,
                              ApplicationEventRepository eventRepository,
                              WorkflowClient workflowClient) {
        this.applicationRepository = applicationRepository;
        this.citizenRepository = citizenRepository;
        this.serviceRepository = serviceRepository;
        this.eventRepository = eventRepository;
        this.workflowClient = workflowClient;
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
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());

        Application savedApplication = applicationRepository.save(application);

        recordEvent(savedApplication, "APPLICATION_SUBMITTED", null, ApplicationStatus.SUBMITTED, "Application created and submitted", request.getCitizenId());

        String workflowKey = service.getWorkflowKey() != null && !service.getWorkflowKey().isBlank()
                ? service.getWorkflowKey()
                : "application-orchestration";

        try {
            workflowClient.startWorkflow(savedApplication.getApplicationNumber(), workflowKey);
            recordEvent(savedApplication, "WORKFLOW_STARTED", ApplicationStatus.SUBMITTED, ApplicationStatus.SUBMITTED,
                    "Workflow started: " + workflowKey, "application-service");
        } catch (Exception e) {
            ApplicationStatus oldStatus = savedApplication.getStatus();
            savedApplication.setStatus(ApplicationStatus.FAILED);
            savedApplication = applicationRepository.save(savedApplication);
            recordEvent(savedApplication, "WORKFLOW_START_FAILED", oldStatus, ApplicationStatus.FAILED,
                    "Workflow start failed", "application-service");
        }

        return mapToResponse(savedApplication);
    }

    @Transactional(readOnly = true)
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

        validateWorkflowTransition(oldStatus, newStatus);

        application.setStatus(newStatus);
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
        response.setSubmittedAt(application.getSubmittedAt());
        return response;
    }
}
