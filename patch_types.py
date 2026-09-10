import re

with open('./frontend/src/types/service.ts', 'r') as f:
    content = f.read()

old_req = """export interface ConsentRequest {
  dataScope: string;
  purpose: string;
  requestingDepartmentId: string;
}"""

new_req = """export interface ConsentRequest {
  applicationId: string;
  serviceCode: string;
  dataScope: string;
  purpose: string;
  requestingDepartmentId: string;
}"""

content = content.replace(old_req, new_req)

with open('./frontend/src/types/service.ts', 'w') as f:
    f.write(content)
