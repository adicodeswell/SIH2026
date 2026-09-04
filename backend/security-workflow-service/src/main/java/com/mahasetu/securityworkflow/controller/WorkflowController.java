package com.mahasetu.securityworkflow.controller;

import com.mahasetu.securityworkflow.dto.WorkflowStartRequest;
import com.mahasetu.securityworkflow.dto.WorkflowStartResponse;
import com.mahasetu.securityworkflow.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/internal/v1/workflows")
    public ResponseEntity<?> startWorkflow(@RequestBody WorkflowStartRequest request) {
        if (request.getApplicationId() == null || request.getApplicationId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("applicationId is required");
        }
        if (request.getWorkflowKey() == null || request.getWorkflowKey().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("workflowKey is required");
        }

        try {
            String processInstanceId = workflowService.startWorkflow(request.getApplicationId(), request.getWorkflowKey());
            return ResponseEntity.ok(new WorkflowStartResponse(processInstanceId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting workflow");
        }
    }
}
