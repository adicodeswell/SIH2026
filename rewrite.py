import re

file_path = 'backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/worker/InteroperabilityWorker.java'
with open(file_path, 'r') as f:
    content = f.read()

replacement = """
        } catch (BpmnError e) {
            throw e;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String safeReason = "INTEROPERABILITY_CLIENT_ERROR_HTTP_" + e.getStatusCode().value();
            log.error("[INTEROP_EVENT] InteroperabilityWorker: client error for applicationId={}, status={}", applicationId, e.getStatusCode());
            execution.setVariable("failureReason", safeReason);
            throw new BpmnError("INTEROP_FETCH_FAILED", safeReason);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            String safeReason = "INTEROPERABILITY_SERVER_ERROR_HTTP_" + e.getStatusCode().value();
            log.error("[INTEROP_EVENT] InteroperabilityWorker: server error for applicationId={}, status={}", applicationId, e.getStatusCode());
            execution.setVariable("failureReason", safeReason);
            throw new BpmnError("INTEROP_FETCH_FAILED", safeReason);
        } catch (Exception e) {
            String safeReason = "INTEROPERABILITY_UNAVAILABLE";
            log.error("[INTEROP_EVENT] InteroperabilityWorker: failed for applicationId={}, type={}",
                    applicationId, e.getClass().getSimpleName());
            execution.setVariable("failureReason", safeReason);
            throw new BpmnError("INTEROP_FETCH_FAILED", safeReason);
        }
    }
}
"""

start_str = "} catch (BpmnError e) {"
idx = content.find(start_str)

if idx != -1:
    new_content = content[:idx] + replacement.strip() + "\n"
    with open(file_path, 'w') as f:
        f.write(new_content)
    print("Replaced catch blocks")
else:
    print("Could not find catch blocks")
