package com.mahasetu.securityworkflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.service.ConsentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.mahasetu.securityworkflow.config.SecurityConfig;

@WebMvcTest(ConsentController.class)
@Import(SecurityConfig.class)
class ConsentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentService consentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCheckConsent_PublicOrInternalAccess() throws Exception {
        when(consentService.checkConsent("c1", "scope", "purpose")).thenReturn(true);

        mockMvc.perform(get("/internal/v1/consents/check")
                .param("citizenId", "c1")
                .param("dataScope", "scope")
                .param("purpose", "purpose"))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckConsent_FailsIfInvalid() throws Exception {
        when(consentService.checkConsent("c1", "scope", "purpose")).thenReturn(false);

        mockMvc.perform(get("/internal/v1/consents/check")
                .param("citizenId", "c1")
                .param("dataScope", "scope")
                .param("purpose", "purpose"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGrantConsent_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "c1", roles = {"CITIZEN"})
    void testGrantConsent_AuthenticatedCitizen() throws Exception {
        ConsentRequest req = new ConsentRequest();
        when(consentService.grantConsent(eq("c1"), any(ConsentRequest.class))).thenReturn(new Consent());

        mockMvc.perform(post("/api/v1/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "c1", roles = {"OFFICER"})
    void testGrantConsent_OfficerForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}
