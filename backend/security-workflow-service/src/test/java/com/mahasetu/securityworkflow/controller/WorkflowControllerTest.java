package com.mahasetu.securityworkflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.securityworkflow.dto.WorkflowStartRequest;
import com.mahasetu.securityworkflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.mahasetu.securityworkflow.config.SecurityConfig;

@WebMvcTest(WorkflowController.class)
@Import(SecurityConfig.class)
public class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowService workflowService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testStartWorkflow_Success() throws Exception {
        WorkflowStartRequest request = new WorkflowStartRequest();
        request.setApplicationId("APP-123");
        request.setWorkflowKey("common-review");

        when(workflowService.startWorkflow("APP-123", "common-review")).thenReturn("PI-456");

        mockMvc.perform(post("/internal/v1/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processInstanceId").value("PI-456"));
    }

    @Test
    public void testStartWorkflow_MissingApplicationId() throws Exception {
        WorkflowStartRequest request = new WorkflowStartRequest();
        request.setWorkflowKey("common-review");

        mockMvc.perform(post("/internal/v1/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testStartWorkflow_MissingWorkflowKey() throws Exception {
        WorkflowStartRequest request = new WorkflowStartRequest();
        request.setApplicationId("APP-123");

        mockMvc.perform(post("/internal/v1/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testStartWorkflow_UnknownKey() throws Exception {
        WorkflowStartRequest request = new WorkflowStartRequest();
        request.setApplicationId("APP-123");
        request.setWorkflowKey("unknown-workflow");

        when(workflowService.startWorkflow("APP-123", "unknown-workflow"))
                .thenThrow(new IllegalArgumentException("Unknown or undeployed workflowKey: unknown-workflow"));

        mockMvc.perform(post("/internal/v1/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
