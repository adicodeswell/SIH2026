package com.mahasetu.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.application.dto.CitizenResponse;
import com.mahasetu.application.dto.CreateCitizenRequest;
import com.mahasetu.application.service.CitizenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitizenController.class)
@AutoConfigureMockMvc
public class CitizenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitizenService citizenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testRegisterCitizen_Success() throws Exception {
        CreateCitizenRequest req = new CreateCitizenRequest();
        req.setCitizenId("MH1001");
        req.setName("Rahul");
        req.setDateOfBirth(LocalDate.of(1995, 1, 1));
        req.setMobile("9876543210");
        req.setEmail("rahul@example.com");

        CitizenResponse res = new CitizenResponse();
        res.setCitizenId("MH1001");
        res.setName("Rahul");

        when(citizenService.createCitizen(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/citizens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.citizenId").value("MH1001"));
    }

    @Test
    @WithMockUser
    void testGetCitizen_Success() throws Exception {
        CitizenResponse res = new CitizenResponse();
        res.setCitizenId("MH1001");

        when(citizenService.getCitizen("MH1001")).thenReturn(res);

        mockMvc.perform(get("/api/v1/citizens/MH1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenId").value("MH1001"));
    }
}
