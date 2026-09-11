import os

for root, _, files in os.walk('backend/security-workflow-service/src/test'):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            if 'new OfficerTaskService(' in content:
                print(f"Fixing {filepath}")
                # We might need to mock ObjectMapper
                if 'import com.fasterxml.jackson.databind.ObjectMapper;' not in content:
                    content = content.replace('import org.junit.jupiter.api.Test;', 'import com.fasterxml.jackson.databind.ObjectMapper;\nimport org.junit.jupiter.api.Test;')
                
                # Check how it's instantiated
                content = content.replace('new OfficerTaskService(taskService, historyService, auditService, consentPolicyService)', 
                                          'new OfficerTaskService(taskService, historyService, auditService, consentPolicyService, new ObjectMapper())')
                
                with open(filepath, 'w') as f:
                    f.write(content)

