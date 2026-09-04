package com.mahasetu.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.WorkflowStatusCallbackRequest;
import com.mahasetu.application.entity.ApplicationStatus;
import com.mahasetu.application.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalApplicationController.class)
@Import(com.mahasetu.application.config.SecurityConfig.class)
class InternalApplicationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @Test
    void workflowCallbackUnauthenticatedReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/internal/v1/applications/MH-2026-000001/workflow-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackJson("PENDING_OFFICER_REVIEW")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void workflowCallbackCitizenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/internal/v1/applications/MH-2026-000001/workflow-status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CITIZEN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackJson("APPROVED")))
                .andExpect(status().isForbidden());
    }

    @Test
    void workflowCallbackServiceRoleAllowed() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setApplicationNumber("MH-2026-000001");
        response.setStatus(ApplicationStatus.PENDING_OFFICER_REVIEW);

        when(applicationService.applyWorkflowStatusCallback(eq("MH-2026-000001"), any(WorkflowStatusCallbackRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/internal/v1/applications/MH-2026-000001/workflow-status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackJson("PENDING_OFFICER_REVIEW")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_OFFICER_REVIEW"));
    }

    private String callbackJson(String status) throws Exception {
        WorkflowStatusCallbackRequest request = new WorkflowStatusCallbackRequest();
        request.setApplicationId("MH-2026-000001");
        request.setProcessInstanceId("proc-1");
        request.setStatus(status);
        return objectMapper.writeValueAsString(request);
    }
}
