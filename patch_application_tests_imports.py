import re

file = 'backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationServiceTest.java'
with open(file, 'r') as f:
    content = f.read()

imports = """
import com.mahasetu.application.integration.WorkflowAuditClient;
import com.mahasetu.application.dto.CitizenApplicationActivityResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
"""

content = content.replace("import java.util.Optional;", imports + "import java.util.Optional;")

mock_decl = """    @Mock
    private WorkflowClient workflowClient;
    @Mock
    private WorkflowAuditClient workflowAuditClient;
"""

content = content.replace("    @Mock\n    private WorkflowClient workflowClient;", mock_decl)

with open(file, 'w') as f:
    f.write(content)

