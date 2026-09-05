package com.mahasetu.securityworkflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mahasetu.securityworkflow.SecurityWorkflowApplication;

@SpringBootTest(classes = SecurityWorkflowApplication.class)
@AutoConfigureMockMvc
public class SecurityTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUnauthenticatedAccess_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/security/test"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthenticatedAccess_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/security/test")
               .with(jwt()))
               .andExpect(status().isOk());
    }

    @Test
    void testActuatorHealth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isOk());
    }

    @Test
    void testCitizenRole_AccessCitizenEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/security/citizen")
               .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CITIZEN"))))
               .andExpect(status().isOk());
    }

    @Test
    void testCitizenRole_AccessOfficerEndpoint_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/security/officer")
               .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CITIZEN"))))
               .andExpect(status().isForbidden());
    }

    @Test
    void testOfficerRole_AccessOfficerEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/security/officer")
               .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
               .andExpect(status().isOk());
    }

    @Test
    void testOfficerRole_AccessAdminEndpoint_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/security/admin")
               .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
               .andExpect(status().isForbidden());
    }

    @Test
    void testAdminRole_AccessAdminEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/security/admin")
               .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
               .andExpect(status().isOk());
    }

    @Test
    void testNoRole_AccessCitizenEndpoint_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/security/citizen")
               .with(jwt()))
               .andExpect(status().isForbidden());
    }
}
