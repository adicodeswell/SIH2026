import re
import os

files_to_fix = [
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/ConsentPolicyServiceTest.java',
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/WorkflowResilienceAndHardeningTest.java',
    'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/ConsentServiceTest.java'
]

for file_path in files_to_fix:
    with open(file_path, 'r') as f:
        content = f.read()

    # Replace new ConsentPolicy(...) using regex with variable arguments
    # Look for: new ConsentPolicy("a", "b", "c") -> append , "", ""
    # This regex looks for ConsentPolicy with exactly 3 string-like arguments
    content = re.sub(
        r'new ConsentPolicy\(\s*("[^"]*")\s*,\s*("[^"]*")\s*,\s*("[^"]*")\s*\)',
        r'new ConsentPolicy(\1, \2, \3, "", "")',
        content
    )

    # For ResolvedConsentPolicy, look for 5 arguments
    content = re.sub(
        r'new ResolvedConsentPolicy\(\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,)]+)\)',
        r'new ResolvedConsentPolicy(\1, \2, \3, \4, \5, java.util.Set.of(), java.util.Set.of())',
        content
    )
    
    # Also fix service.getPolicy("SRV-1", "user", "ip") if it got mangled
    content = re.sub(
        r'service\.getPolicy\(\s*([^,]+),\s*("[^"]*")\s*,\s*("[^"]*")\s*\)',
        r'service.getPolicy(\1)',
        content
    )

    with open(file_path, 'w') as f:
        f.write(content)
