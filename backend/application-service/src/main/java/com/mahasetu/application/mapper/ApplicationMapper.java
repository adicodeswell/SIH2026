package com.mahasetu.application.mapper;

import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.TimelineEventResponse;
import com.mahasetu.application.entity.Application;
import com.mahasetu.application.entity.ApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponse toResponse(Application application) {
        if (application == null) return null;
        ApplicationResponse response = new ApplicationResponse();
        response.setApplicationNumber(application.getApplicationNumber());
        response.setStatus(application.getStatus());
        if (application.getCitizen() != null) {
            response.setCitizenId(application.getCitizen().getCitizenId());
        }
        if (application.getService() != null) {
            response.setServiceCode(application.getService().getServiceCode());
        }
        response.setSubmittedAt(application.getSubmittedAt());
        return response;
    }

    public TimelineEventResponse toTimelineEventResponse(ApplicationEvent event) {
        if (event == null) return null;
        TimelineEventResponse response = new TimelineEventResponse();
        response.setEventType(event.getEventType());
        response.setDescription(event.getDescription());
        response.setOccurredAt(event.getOccurredAt());
        return response;
    }
}
