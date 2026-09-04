package com.mahasetu.securityworkflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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
}
