package com.mahasetu.application.service;

import com.mahasetu.application.dto.*;
import com.mahasetu.application.entity.*;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.exception.ValidationException;
import com.mahasetu.application.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CitizenRepository citizenRepository;
    private final ServiceRepository serviceRepository;
    private final ApplicationEventRepository eventRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              CitizenRepository citizenRepository,
                              ServiceRepository serviceRepository,
                              ApplicationEventRepository eventRepository) {
        this.applicationRepository = applicationRepository;
        this.citizenRepository = citizenRepository;
        this.serviceRepository = serviceRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
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
            case SUBMITTED -> newStatus == ApplicationStatus.IN_PROGRESS || newStatus == ApplicationStatus.PENDING_VERIFICATION || newStatus == ApplicationStatus.CANCELLED;
            case PENDING_VERIFICATION -> newStatus == ApplicationStatus.IN_PROGRESS || newStatus == ApplicationStatus.PENDING_REVIEW || newStatus == ApplicationStatus.FAILED;
            case PENDING_REVIEW -> newStatus == ApplicationStatus.APPROVED || newStatus == ApplicationStatus.REJECTED;
            case APPROVED -> newStatus == ApplicationStatus.COMPLETED;
            case REJECTED, FAILED, CANCELLED, COMPLETED -> false; // Terminal states
            case IN_PROGRESS -> newStatus == ApplicationStatus.PENDING_VERIFICATION || newStatus == ApplicationStatus.PENDING_REVIEW || newStatus == ApplicationStatus.CANCELLED;
        };

        if (!isValid) {
            throw new ValidationException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }
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
