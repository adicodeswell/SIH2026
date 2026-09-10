import re

file_path = 'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/WorkflowResilienceAndHardeningTest.java'
with open(file_path, 'r') as f:
    content = f.read()

test_to_insert = """
    @Test
    void testInteropWorker_ExceptionMasking_NoSensitiveDataPropagated() throws Exception {
        when(execution.getVariable("citizenId")).thenReturn("CIT-123");
        when(execution.getVariable("applicationId")).thenReturn("APP-123");
        when(execution.getVariable("allowedScopes")).thenReturn(List.of("education"));

        // Simulate a downstream error with a highly sensitive exception message
        String sensitiveMessage = "Aadhaar=123456789012 bankAccount=1234567890 income=500000";
        HttpClientErrorException exceptionWithSensitiveData = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, sensitiveMessage, HttpHeaders.EMPTY, null, null);

        when(interoperabilityClient.fetchScopedData("CIT-123", List.of("education")))
                .thenThrow(exceptionWithSensitiveData);

        BpmnError thrown = assertThrows(BpmnError.class, () -> {
            interopWorker.execute(execution);
        });

        // Verify the exception itself doesn't contain the sensitive text
        assertFalse(thrown.getMessage().contains("Aadhaar="));
        assertEquals("INTEROP_FETCH_FAILED", thrown.getErrorCode());
        
        // Verify execution variable "failureReason" was set to a safe category, NOT the raw message
        verify(execution).setVariable(eq("failureReason"), argThat(reason -> {
            String r = (String) reason;
            return r.contains("INTEROPERABILITY_CLIENT_ERROR") && !r.contains("Aadhaar=");
        }));
    }
"""

idx = content.find("// 1. Interoperability Failure & Resilience Tests")
if idx != -1:
    idx += len("// 1. Interoperability Failure & Resilience Tests")
    idx = content.find("=", idx) + 1  # pass the ==== line
    new_content = content[:idx] + "\n" + test_to_insert + "\n" + content[idx:]
    with open(file_path, 'w') as f:
        f.write(new_content)
    print("Added test successfully")
else:
    print("Could not find insertion point")
