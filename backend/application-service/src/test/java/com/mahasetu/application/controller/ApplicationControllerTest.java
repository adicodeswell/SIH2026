package com.mahasetu.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.CreateApplicationRequest;
import com.mahasetu.application.entity.ApplicationStatus;
import com.mahasetu.application.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private com.mahasetu.application.integration.ConsentClient consentClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CITIZEN")
    void testCreateApplication_Success() throws Exception {
        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setCitizenId("MH1001");
        req.setServiceCode("SKILL_BENEFIT");

        ApplicationResponse res = new ApplicationResponse();
        res.setApplicationNumber("MH-2026-000001");
        res.setStatus(ApplicationStatus.SUBMITTED);
        res.setCitizenId("MH1001");
        res.setServiceCode("SKILL_BENEFIT");

        when(applicationService.createApplication(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/applications")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationNumber").value("MH-2026-000001"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }
}
