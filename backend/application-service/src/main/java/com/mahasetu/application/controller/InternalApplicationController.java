package com.mahasetu.application.controller;

import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.WorkflowStatusCallbackRequest;
import com.mahasetu.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalApplicationController {

    private final ApplicationService applicationService;

    public InternalApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/internal/v1/applications/{applicationId}/workflow-status")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<ApplicationResponse> receiveWorkflowStatus(
            @PathVariable String applicationId,
            @Valid @RequestBody WorkflowStatusCallbackRequest request) {
        return ResponseEntity.ok(applicationService.applyWorkflowStatusCallback(applicationId, request));
    }
}
