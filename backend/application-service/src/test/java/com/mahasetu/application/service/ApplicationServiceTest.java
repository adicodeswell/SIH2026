package com.mahasetu.application.service;

import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.CreateApplicationRequest;
import com.mahasetu.application.dto.UpdateApplicationStatusRequest;
import com.mahasetu.application.dto.WorkflowStatusCallbackRequest;
import com.mahasetu.application.entity.*;
import com.mahasetu.application.exception.ValidationException;
import com.mahasetu.application.integration.WorkflowClient;
import com.mahasetu.application.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private CitizenRepository citizenRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private ApplicationEventRepository eventRepository;
    @Mock
    private WorkflowClient workflowClient;

    @InjectMocks
    private ApplicationService applicationService;

    private Citizen citizen;
    private com.mahasetu.application.entity.Service serviceEntity;
    private Application existingApp;

    @BeforeEach
    void setUp() {
        citizen = new Citizen();
        citizen.setCitizenId("MH1001");
        
        Department dept = new Department();
        dept.setDepartmentCode("DEPT_1");
        
        serviceEntity = new com.mahasetu.application.entity.Service();
        serviceEntity.setServiceCode("SKILL_BENEFIT");
        serviceEntity.setActive(true);
        serviceEntity.setDepartment(dept);
        serviceEntity.setWorkflowKey("application-orchestration");

        existingApp = new Application();
        existingApp.setApplicationNumber("MH-2026-000001");
        existingApp.setCitizen(citizen);
        existingApp.setService(serviceEntity);
        existingApp.setStatus(ApplicationStatus.SUBMITTED);
    }

    @Test
    void testCreateApplication_Success() {
        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setCitizenId("MH1001");
        req.setServiceCode("SKILL_BENEFIT");

        when(citizenRepository.findByCitizenId("MH1001")).thenReturn(Optional.of(citizen));
        when(serviceRepository.findByServiceCode("SKILL_BENEFIT")).thenReturn(Optional.of(serviceEntity));
        
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApp);

        ApplicationResponse res = applicationService.createApplication(req);

        assertNotNull(res);
        assertEquals("MH-2026-000001", res.getApplicationNumber());
        assertEquals(ApplicationStatus.SUBMITTED, res.getStatus());
        verify(workflowClient).startWorkflow("MH-2026-000001", "application-orchestration");
        verify(eventRepository, times(2)).save(any(ApplicationEvent.class));
    }

    @Test
    void testCreateApplication_WorkflowStartFailureMarksApplicationFailed() {
        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setCitizenId("MH1001");
        req.setServiceCode("SKILL_BENEFIT");

        when(citizenRepository.findByCitizenId("MH1001")).thenReturn(Optional.of(citizen));
        when(serviceRepository.findByServiceCode("SKILL_BENEFIT")).thenReturn(Optional.of(serviceEntity));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("workflow unavailable"))
                .when(workflowClient).startWorkflow(anyString(), eq("application-orchestration"));

        ApplicationResponse res = applicationService.createApplication(req);

        assertEquals(ApplicationStatus.FAILED, res.getStatus());
        verify(eventRepository, times(2)).save(any(ApplicationEvent.class));
    }
    
    @Test
    void testCreateApplication_InactiveService() {
        serviceEntity.setActive(false);
        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setCitizenId("MH1001");
        req.setServiceCode("SKILL_BENEFIT");

        when(citizenRepository.findByCitizenId("MH1001")).thenReturn(Optional.of(citizen));
        when(serviceRepository.findByServiceCode("SKILL_BENEFIT")).thenReturn(Optional.of(serviceEntity));

        assertThrows(ValidationException.class, () -> applicationService.createApplication(req));
    }

    @Test
    void testStatusTransition_ValidSubmittedToInProgress() {
        UpdateApplicationStatusRequest req = new UpdateApplicationStatusRequest();
        req.setStatus(ApplicationStatus.IN_PROGRESS);
        req.setDescription("Processing started");
        req.setPerformedBy("Officer 1");

        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApp);

        ApplicationResponse response = applicationService.updateApplicationStatus("MH-2026-000001", req);
        assertNotNull(response);
        assertEquals(ApplicationStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void testStatusTransition_InvalidSubmittedToApproved() {
        UpdateApplicationStatusRequest req = new UpdateApplicationStatusRequest();
        req.setStatus(ApplicationStatus.APPROVED);

        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));

        assertThrows(ValidationException.class, () -> applicationService.updateApplicationStatus("MH-2026-000001", req));
    }

    @Test
    void testStatusTransition_TerminalStateCannotBeChanged() {
        existingApp.setStatus(ApplicationStatus.COMPLETED);
        UpdateApplicationStatusRequest req = new UpdateApplicationStatusRequest();
        req.setStatus(ApplicationStatus.IN_PROGRESS);

        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));

        assertThrows(ValidationException.class, () -> applicationService.updateApplicationStatus("MH-2026-000001", req));
    }

    @Test
    void testWorkflowCallbackPendingReviewUpdatesStatusAndEvent() {
        WorkflowStatusCallbackRequest req = new WorkflowStatusCallbackRequest();
        req.setApplicationId("MH-2026-000001");
        req.setProcessInstanceId("proc-1");
        req.setStatus("PENDING_OFFICER_REVIEW");

        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApp);

        ApplicationResponse response = applicationService.applyWorkflowStatusCallback("MH-2026-000001", req);

        assertEquals(ApplicationStatus.PENDING_OFFICER_REVIEW, response.getStatus());
        verify(eventRepository).save(argThat(event -> "WORKFLOW_PENDING_REVIEW".equals(event.getEventType())));
    }

    @Test
    void testWorkflowCallbackApprovedFromPendingReview() {
        existingApp.setStatus(ApplicationStatus.PENDING_OFFICER_REVIEW);
        WorkflowStatusCallbackRequest req = new WorkflowStatusCallbackRequest();
        req.setApplicationId("MH-2026-000001");
        req.setProcessInstanceId("proc-1");
        req.setStatus("APPROVED");
        req.setOfficerId("officer_1");

        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApp);

        ApplicationResponse response = applicationService.applyWorkflowStatusCallback("MH-2026-000001", req);

        assertEquals(ApplicationStatus.APPROVED, response.getStatus());
        verify(eventRepository).save(argThat(event -> "WORKFLOW_APPROVED".equals(event.getEventType())));
    }

    @Test
    void testWorkflowCallbackDuplicateDoesNotCreateDuplicateEvent() {
        existingApp.setStatus(ApplicationStatus.PENDING_OFFICER_REVIEW);
        WorkflowStatusCallbackRequest req = new WorkflowStatusCallbackRequest();
        req.setApplicationId("MH-2026-000001");
        req.setProcessInstanceId("proc-1");
        req.setStatus("PENDING_OFFICER_REVIEW");

        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));

        ApplicationResponse response = applicationService.applyWorkflowStatusCallback("MH-2026-000001", req);

        assertEquals(ApplicationStatus.PENDING_OFFICER_REVIEW, response.getStatus());
        verify(applicationRepository, never()).save(any(Application.class));
        verify(eventRepository, never()).save(any(ApplicationEvent.class));
    }

    @Test
    void testWorkflowCallbackInvalidTransitionRejected() {
        existingApp.setStatus(ApplicationStatus.APPROVED);
        WorkflowStatusCallbackRequest req = new WorkflowStatusCallbackRequest();
        req.setApplicationId("MH-2026-000001");
        req.setProcessInstanceId("proc-1");
        req.setStatus("PENDING_OFFICER_REVIEW");

        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));

        ApplicationResponse response = applicationService.applyWorkflowStatusCallback("MH-2026-000001", req);
        assertEquals(ApplicationStatus.APPROVED, response.getStatus());
        verify(applicationRepository, never()).save(any(Application.class));
    }
}
