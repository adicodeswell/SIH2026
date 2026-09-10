package com.mahasetu.securityworkflow.controller;

import com.mahasetu.securityworkflow.dto.OfficerDecisionResponse;
import com.mahasetu.securityworkflow.dto.OfficerReviewTaskResponse;
import com.mahasetu.securityworkflow.service.OfficerTaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mahasetu.securityworkflow.SecurityWorkflowApplication;

@SpringBootTest(classes = SecurityWorkflowApplication.class)
@AutoConfigureMockMvc
public class OfficerReviewControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    

    private OfficerTaskService officerTaskService;

    @Test
    void testGetPendingReviews_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/officer/reviews"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetPendingReviews_CitizenRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/officer/reviews")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CITIZEN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetPendingReviews_NoRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/officer/reviews")
                .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetPendingReviews_OfficerRole_ReturnsOk() throws Exception {
        OfficerReviewTaskResponse task = new OfficerReviewTaskResponse(
                "task-123", "Officer Review", "APP-1", "proc-1",
                "CIT-1", "SRV-EDU", new Date(), "OFFICER", null, "PENDING_REVIEW"
        );
        when(officerTaskService.getPendingOfficerTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/officer/reviews")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value("task-123"))
                .andExpect(jsonPath("$[0].applicationId").value("APP-1"));
    }

    @Test
    void testGetPendingReviews_AdminRole_ReturnsOk() throws Exception {
        when(officerTaskService.getPendingOfficerTasks()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/officer/reviews")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitDecision_CitizenRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CITIZEN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "decision": "APPROVE"
                        }
                        """))
                .andExpect(status().isForbidden());
    }


    @Test
    void testSubmitDecision_ValidPreferredUsername_ExtractsCorrectly() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "APPROVE", "officer123", null, LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), eq("officer123"), eq("SKILLS"), eq("APPROVE"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))
                        .jwt(j -> j.claim("department", "SKILLS").claim("preferred_username", "officer123")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\": \"APPROVE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitDecision_EmptyPreferredUsernameValidFallback_ExtractsCorrectly() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "APPROVE", "fallbackUser", null, LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), eq("fallbackUser"), eq("SKILLS"), eq("APPROVE"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))
                        .jwt(j -> j.claim("department", "SKILLS")
                                .claim("preferred_username", "")
                                .subject("fallbackUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\": \"APPROVE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitDecision_WhitespacePreferredUsernameValidFallback_ExtractsCorrectly() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "APPROVE", "fallbackUser", null, LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), eq("fallbackUser"), eq("SKILLS"), eq("APPROVE"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))
                        .jwt(j -> j.claim("department", "SKILLS")
                                .claim("preferred_username", "   ")
                                .subject("fallbackUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\": \"APPROVE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitDecision_OfficerRole_Allowed() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "APPROVE", "officer_42", null, LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), anyString(), eq("SKILLS"), eq("APPROVE"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))
                        .jwt(j -> j.claim("department", "SKILLS").subject("officer_42")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "decision": "APPROVE"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVE"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testSubmitDecision_AdminRole_Allowed() throws Exception {
        OfficerDecisionResponse response = new OfficerDecisionResponse(
                "task-123", "APP-1", "REJECT", "admin_1", "Fraudulent documents", LocalDateTime.now(), "COMPLETED"
        );
        when(officerTaskService.completeOfficerDecision(eq("task-123"), anyString(), eq("SKILLS"), eq("REJECT"), eq("Fraudulent documents")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/officer/reviews/task-123/decision")
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        .jwt(j -> j.claim("department", "SKILLS").subject("admin_1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "decision": "REJECT",
                          "reason": "Fraudulent documents"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("REJECT"))
                .andExpect(jsonPath("$.reason").value("Fraudulent documents"));
    }
}
