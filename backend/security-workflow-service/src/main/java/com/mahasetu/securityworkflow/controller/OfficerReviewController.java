package com.mahasetu.securityworkflow.controller;

import com.mahasetu.securityworkflow.dto.OfficerDecisionRequest;
import com.mahasetu.securityworkflow.dto.OfficerDecisionResponse;
import com.mahasetu.securityworkflow.dto.OfficerReviewTaskResponse;
import com.mahasetu.securityworkflow.service.OfficerTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for government officers and administrators to interact with pending review tasks.
 * Secured strictly with ROLE_OFFICER and ROLE_ADMIN.
 * Officer identity is extracted solely from the authenticated SecurityContext.
 */
@RestController
@RequestMapping("/api/v1/officer/reviews")
@PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
public class OfficerReviewController {

    private final OfficerTaskService officerTaskService;

    public OfficerReviewController(OfficerTaskService officerTaskService) {
        this.officerTaskService = officerTaskService;
    }

    /**
     * Lists all pending officer review tasks.
     */
    @GetMapping
    public ResponseEntity<List<OfficerReviewTaskResponse>> getPendingReviews() {
        return ResponseEntity.ok(officerTaskService.getPendingOfficerTasks());
    }

    /**
     * Retrieves details for a specific officer review task.
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<OfficerReviewTaskResponse> getReviewTask(@PathVariable String taskId) {
        return ResponseEntity.ok(officerTaskService.getOfficerTaskById(taskId));
    }

    /**
     * Claims a pending review task for the authenticated officer.
     */
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<OfficerReviewTaskResponse> claimTask(
            @PathVariable String taskId,
            Authentication authentication) {
        String officerId = authentication.getName();
        return ResponseEntity.ok(officerTaskService.claimTask(taskId, officerId));
    }

    /**
     * Unclaims a review task previously claimed by the authenticated officer.
     */
    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<OfficerReviewTaskResponse> unclaimTask(
            @PathVariable String taskId,
            Authentication authentication) {
        String officerId = authentication.getName();
        return ResponseEntity.ok(officerTaskService.unclaimTask(taskId, officerId));
    }

    /**
     * Submits an officer review decision (APPROVE or REJECT).
     * The officer identity is bound from the authenticated JWT token.
     */
    @PostMapping("/{taskId}/decision")
    public ResponseEntity<OfficerDecisionResponse> submitDecision(
            @PathVariable String taskId,
            @Valid @RequestBody OfficerDecisionRequest request,
            Authentication authentication) {
        String officerId = authentication.getName();
        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(
                taskId,
                officerId,
                request.getDecision(),
                request.getReason()
        );
        return ResponseEntity.ok(response);
    }
}
