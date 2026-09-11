import re

file = 'backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationServiceTest.java'
with open(file, 'r') as f:
    content = f.read()

tests_to_add = """

    @Test
    void testGetApplicationActivity_ApproveAuditLog() {
        existingApp.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));
        when(eventRepository.findByApplication_ApplicationNumberOrderByOccurredAtAsc("MH-2026-000001")).thenReturn(Collections.emptyList());

        Map<String, Object> auditLog = new HashMap<>();
        auditLog.put("action", "OFFICER_REVIEW");
        auditLog.put("purpose", "APPROVE");
        auditLog.put("metadata", "{\\"reason\\":\\"Everything was fine\\"}");
        auditLog.put("occurredAt", LocalDateTime.now().toString());
        auditLog.put("id", UUID.randomUUID().toString());

        when(workflowAuditClient.getApplicationAuditLogs("MH-2026-000001")).thenReturn(List.of(auditLog));

        List<CitizenApplicationActivityResponse> activities = applicationService.getApplicationActivity("MH-2026-000001");
        
        assertEquals(1, activities.size());
        assertEquals("Application Approved", activities.get(0).getTitle());
        assertEquals("APPROVED", activities.get(0).getStatus());
        assertEquals("Everything was fine", activities.get(0).getDescription());
    }

    @Test
    void testGetApplicationActivity_RejectAuditLog() {
        existingApp.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));
        when(eventRepository.findByApplication_ApplicationNumberOrderByOccurredAtAsc("MH-2026-000001")).thenReturn(Collections.emptyList());

        Map<String, Object> auditLog = new HashMap<>();
        auditLog.put("action", "OFFICER_REVIEW");
        auditLog.put("purpose", "REJECT");
        auditLog.put("metadata", "{\\"reason\\":\\"Eligibility condition failed\\"}");
        auditLog.put("occurredAt", LocalDateTime.now().toString());
        auditLog.put("id", UUID.randomUUID().toString());

        when(workflowAuditClient.getApplicationAuditLogs("MH-2026-000001")).thenReturn(List.of(auditLog));

        List<CitizenApplicationActivityResponse> activities = applicationService.getApplicationActivity("MH-2026-000001");
        
        assertEquals(1, activities.size());
        assertEquals("Application Rejected", activities.get(0).getTitle());
        assertEquals("REJECTED", activities.get(0).getStatus());
        assertEquals("Eligibility condition failed", activities.get(0).getDescription());
    }

    @Test
    void testGetApplicationActivity_ApproveNoReason() {
        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));
        when(eventRepository.findByApplication_ApplicationNumberOrderByOccurredAtAsc("MH-2026-000001")).thenReturn(Collections.emptyList());

        Map<String, Object> auditLog = new HashMap<>();
        auditLog.put("action", "OFFICER_REVIEW");
        auditLog.put("purpose", "APPROVE");
        auditLog.put("metadata", "{\\"taskId\\":\\"123\\"}");
        auditLog.put("occurredAt", LocalDateTime.now().toString());
        auditLog.put("id", UUID.randomUUID().toString());

        when(workflowAuditClient.getApplicationAuditLogs("MH-2026-000001")).thenReturn(List.of(auditLog));

        List<CitizenApplicationActivityResponse> activities = applicationService.getApplicationActivity("MH-2026-000001");
        
        assertEquals("Application approved by the reviewing officer.", activities.get(0).getDescription());
    }

    @Test
    void testGetApplicationActivity_MalformedMetadata() {
        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));
        when(eventRepository.findByApplication_ApplicationNumberOrderByOccurredAtAsc("MH-2026-000001")).thenReturn(Collections.emptyList());

        Map<String, Object> auditLog = new HashMap<>();
        auditLog.put("action", "OFFICER_REVIEW");
        auditLog.put("purpose", "APPROVE");
        auditLog.put("metadata", "invalid json {");
        auditLog.put("occurredAt", LocalDateTime.now().toString());
        auditLog.put("id", UUID.randomUUID().toString());

        when(workflowAuditClient.getApplicationAuditLogs("MH-2026-000001")).thenReturn(List.of(auditLog));

        List<CitizenApplicationActivityResponse> activities = applicationService.getApplicationActivity("MH-2026-000001");
        
        assertEquals("Application Approved", activities.get(0).getTitle());
        assertEquals("Application approved by the reviewing officer.", activities.get(0).getDescription());
    }

    @Test
    void testApplyWorkflowStatusCallback_ApproveWithReason() {
        existingApp.setStatus(ApplicationStatus.PENDING_OFFICER_REVIEW);
        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));

        WorkflowStatusCallbackRequest req = new WorkflowStatusCallbackRequest();
        req.setStatus("APPROVED");
        req.setOfficerDecision("APPROVE");
        req.setOfficerDecisionReason("Approval note");

        applicationService.applyWorkflowStatusCallback("MH-2026-000001", req);
        
        verify(eventRepository).save(argThat(event -> "WORKFLOW_APPROVED".equals(event.getEventType()) && "Approval note".equals(event.getDescription())));
    }

    @Test
    void testApplyWorkflowStatusCallback_RejectWithReason() {
        existingApp.setStatus(ApplicationStatus.PENDING_OFFICER_REVIEW);
        when(applicationRepository.findByApplicationNumber("MH-2026-000001")).thenReturn(Optional.of(existingApp));

        WorkflowStatusCallbackRequest req = new WorkflowStatusCallbackRequest();
        req.setStatus("REJECTED");
        req.setOfficerDecision("REJECT");
        req.setOfficerDecisionReason("Rejection reason");

        applicationService.applyWorkflowStatusCallback("MH-2026-000001", req);
        
        verify(eventRepository).save(argThat(event -> "WORKFLOW_REJECTED".equals(event.getEventType()) && "Rejection reason".equals(event.getDescription())));
    }

"""

# Find the last closing brace
last_brace_idx = content.rfind('}')
if last_brace_idx != -1:
    content = content[:last_brace_idx] + tests_to_add + "\n}" + content[last_brace_idx+1:]

with open(file, 'w') as f:
    f.write(content)

