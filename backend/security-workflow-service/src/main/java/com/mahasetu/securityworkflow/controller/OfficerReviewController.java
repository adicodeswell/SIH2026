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
    public ResponseEntity<List<OfficerReviewTaskResponse>> getPendingReviews(Authentication authentication) {
        String officerDepartment = extractOfficerDepartment(authentication);
        return ResponseEntity.ok(officerTaskService.getPendingOfficerTasks(officerDepartment));
    }

    /**
     * Retrieves details for a specific officer review task.
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<OfficerReviewTaskResponse> getReviewTask(@PathVariable String taskId, Authentication authentication) {
        String officerDepartment = extractOfficerDepartment(authentication);
        return ResponseEntity.ok(officerTaskService.getOfficerTaskById(taskId, officerDepartment));
    }


    private String extractOfficerDepartment(Authentication authentication) {
        if (!(authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth)) {
            throw new org.springframework.security.access.AccessDeniedException("Officer department not found");
        }

        Object deptClaim = jwtAuth.getToken().getClaim("department");
        String department = null;
        if (deptClaim instanceof java.util.Collection collection) {
            if (!collection.isEmpty()) {
                department = String.valueOf(collection.iterator().next());
            }
        } else if (deptClaim instanceof String str) {
            department = str;
        } else if (deptClaim != null) {
            department = String.valueOf(deptClaim);
        }

        if (department == null || department.trim().isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Officer department not found");
        }

        // Clean up brackets if Keycloak serialized array as string e.g. "["DEPT-SKILLS"]"
        department = department.replaceAll("^\\[\"?|\"?\\]$", "");
        
        return department.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String extractUserId(Authentication authentication) {
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String username = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (username != null && !username.trim().isEmpty()) {
                return username.trim();
            }
        }
        String name = authentication.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Officer identity not found");
        }
        return name.trim();
    }

    /**
     * Claims a pending review task for the authenticated officer.
     */
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<OfficerReviewTaskResponse> claimTask(
            @PathVariable String taskId,
            Authentication authentication) {
        String officerId = extractUserId(authentication);
        String officerDepartment = extractOfficerDepartment(authentication);
        return ResponseEntity.ok(officerTaskService.claimTask(taskId, officerId, officerDepartment));
    }

    /**
     * Unclaims a review task previously claimed by the authenticated officer.
     */
    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<OfficerReviewTaskResponse> unclaimTask(
            @PathVariable String taskId,
            Authentication authentication) {
        String officerId = extractUserId(authentication);
        String officerDepartment = extractOfficerDepartment(authentication);
        return ResponseEntity.ok(officerTaskService.unclaimTask(taskId, officerId, officerDepartment));
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
        String officerId = extractUserId(authentication);
        String officerDepartment = extractOfficerDepartment(authentication);
        OfficerDecisionResponse response = officerTaskService.completeOfficerDecision(
                taskId,
                officerId,
                officerDepartment,
                request.getDecision(),
                request.getReason()
        );
        return ResponseEntity.ok(response);
    }
}
