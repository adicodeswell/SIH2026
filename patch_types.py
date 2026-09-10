with open('./frontend/src/types/service.ts', 'r') as f:
    content = f.read()

new_types = """
export interface CitizenApplicationSummaryResponse {
  applicationNumber: string;
  serviceCode: string;
  serviceName: string;
  departmentCode: string;
  departmentName: string;
  status: string;
  createdAt: string;
  submittedAt: string | null;
  updatedAt: string | null;
}

export interface CitizenApplicationActivityResponse {
  id: string;
  type: string;
  category: string;
  title: string;
  description: string;
  status: string | null;
  occurredAt: string;
  actorType: string;
}
"""

if "CitizenApplicationSummaryResponse" not in content:
    content = content + new_types

with open('./frontend/src/types/service.ts', 'w') as f:
    f.write(content)
