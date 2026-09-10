package com.mahasetu.application.service;

import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.CreateApplicationRequest;
import com.mahasetu.application.entity.*;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.exception.ValidationException;
import com.mahasetu.application.integration.WorkflowClient;
import com.mahasetu.application.repository.ApplicationEventRepository;
import com.mahasetu.application.repository.ApplicationRepository;
import com.mahasetu.application.repository.CitizenRepository;
import com.mahasetu.application.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationWorkflowRecoveryTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationEventRepository eventRepository;
    @Mock
    private CitizenRepository citizenRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private WorkflowClient workflowClient;
    @org.mockito.Mock
    private com.mahasetu.application.integration.ConsentClient consentClient;

    @InjectMocks
    private ApplicationService applicationService;

    private Citizen citizen;
    private Service service;
    private Application application;

    @BeforeEach
    void setUp() {
        citizen = new Citizen();
        citizen.setCitizenId("CIT-999");
        citizen.setName("Recovery Test Citizen");
        citizen.setDateOfBirth(LocalDate.of(1995, 5, 15));

        Department department = new Department();
        department.setDepartmentCode("DEPT_REV");
        department.setName("Revenue Department");

        service = new Service();
        service.setServiceCode("INCOME_CERT");
        service.setServiceName("Income Certificate");
        service.setActive(true);
        service.setDepartment(department);
        service.setWorkflowKey("application-orchestration");

        application = new Application();
        application.setApplicationNumber("MH-2026-REC001");
        application.setCitizen(citizen);
        application.setService(service);
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());
    }

    @Test
    void testCreateApplication_WorkflowStartFails_ApplicationMarkedFailedWithEvent() {
        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setCitizenId("CIT-999");
        req.setServiceCode("INCOME_CERT");

        when(citizenRepository.findByCitizenId("CIT-999")).thenReturn(Optional.of(citizen));
        when(serviceRepository.findByServiceCode("INCOME_CERT")).thenReturn(Optional.of(service));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        // WorkflowClient throws exception (e.g. connection refused)
        doThrow(new RuntimeException("Connection refused to workflow service"))
                .when(workflowClient).startWorkflow(any(), eq("application-orchestration"));

        ApplicationResponse res = applicationService.createApplication(req);

        assertNotNull(res);
        assertEquals(ApplicationStatus.FAILED, res.getStatus());

        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventRepository, atLeast(2)).save(eventCaptor.capture());

        boolean hasFailedEvent = eventCaptor.getAllValues().stream()
                .anyMatch(e -> "WORKFLOW_START_FAILED".equals(e.getEventType()) && e.getNewStatus() == ApplicationStatus.FAILED);
        assertTrue(hasFailedEvent, "Should have recorded WORKFLOW_START_FAILED event");
    }

    @Test
    void testRetryWorkflow_OnFailedApplication_SucceedsAndRecoversToSubmitted() {
        application.setStatus(ApplicationStatus.FAILED);
        when(applicationRepository.findByApplicationNumber("MH-2026-REC001")).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        // Workflow client succeeds on retry
        doNothing().when(workflowClient).startWorkflow("MH-2026-REC001", "application-orchestration");

        ApplicationResponse res = applicationService.retryWorkflow("MH-2026-REC001");

        assertNotNull(res);
        assertEquals(ApplicationStatus.SUBMITTED, res.getStatus());
        verify(workflowClient).startWorkflow("MH-2026-REC001", "application-orchestration");

        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventRepository).save(eventCaptor.capture());

        ApplicationEvent event = eventCaptor.getValue();
        assertEquals("WORKFLOW_RETRY_SUCCEEDED", event.getEventType());
        assertEquals(ApplicationStatus.SUBMITTED, event.getNewStatus());
        assertEquals(ApplicationStatus.FAILED, event.getOldStatus());
    }

    @Test
    void testRetryWorkflow_WhenWorkflowClientFailsAgain_RemainsFailedAndRecordsEvent() {
        application.setStatus(ApplicationStatus.FAILED);
        when(applicationRepository.findByApplicationNumber("MH-2026-REC001")).thenReturn(Optional.of(application));

        // Workflow client fails again
        doThrow(new RuntimeException("Network timeout"))
                .when(workflowClient).startWorkflow("MH-2026-REC001", "application-orchestration");

        assertThrows(RuntimeException.class, () -> applicationService.retryWorkflow("MH-2026-REC001"));

        assertEquals(ApplicationStatus.FAILED, application.getStatus());
        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventRepository).save(eventCaptor.capture());

        ApplicationEvent event = eventCaptor.getValue();
        assertEquals("WORKFLOW_RETRY_FAILED", event.getEventType());
        assertEquals(ApplicationStatus.FAILED, event.getNewStatus());
    }

    @Test
    void testRetryWorkflow_OnNonRecoverableStatus_ThrowsValidationException() {
        application.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findByApplicationNumber("MH-2026-REC001")).thenReturn(Optional.of(application));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> applicationService.retryWorkflow("MH-2026-REC001"));
        assertTrue(ex.getMessage().contains("Cannot retry workflow for application in status: APPROVED"));
        verify(workflowClient, never()).startWorkflow(any(), any());
    }

    @Test
    void testRetryWorkflow_ApplicationNotFound_ThrowsResourceNotFoundException() {
        when(applicationRepository.findByApplicationNumber("MH-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.retryWorkflow("MH-UNKNOWN"));
    }
}
