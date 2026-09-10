import re

with open('./backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationControllerTest.java', 'r') as f:
    content = f.read()

import_statement = "import org.springframework.boot.test.mock.mockito.MockBean;"
consent_mock = "@MockBean\n    private com.mahasetu.application.integration.ConsentClient consentClient;"

if "ConsentClient consentClient;" not in content:
    content = content.replace("private ApplicationService applicationService;", "private ApplicationService applicationService;\n    " + consent_mock)

with open('./backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationControllerTest.java', 'w') as f:
    f.write(content)
