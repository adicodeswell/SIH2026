with open('./frontend/src/types/service.ts', 'r') as f:
    content = f.read()

import re

# Update ApplicationResponse
if "serviceName: string;" not in content:
    content = re.sub(
        r'export interface ApplicationResponse \{([^\}]+)\}',
        r'export interface ApplicationResponse {\1  serviceName: string;\n  departmentCode: string;\n  createdAt: string;\n  updatedAt: string | null;\n}',
        content
    )

with open('./frontend/src/types/service.ts', 'w') as f:
    f.write(content)
