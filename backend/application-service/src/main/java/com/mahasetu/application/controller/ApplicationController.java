package com.mahasetu.application.controller;

import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.CreateApplicationRequest;
import com.mahasetu.application.dto.TimelineEventResponse;
import com.mahasetu.application.dto.UpdateApplicationStatusRequest;
import com.mahasetu.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        return applicationService.createApplication(request);
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplicationDetails(@PathVariable("id") String id) {
        return applicationService.getApplication(id);
    }

    @GetMapping("/{id}/status")
    public ApplicationResponse getApplicationStatus(@PathVariable("id") String id) {
        // According to contract, returns similar structure or just status.
        return applicationService.getApplication(id);
    }

    @GetMapping("/{id}/timeline")
    public List<TimelineEventResponse> getApplicationTimeline(@PathVariable("id") String id) {
        return applicationService.getApplicationTimeline(id);
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse updateApplicationStatus(@PathVariable("id") String id, @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateApplicationStatus(id, request);
    }
}
