import re

with open('./frontend/src/services/serviceCatalog.ts', 'r') as f:
    content = f.read()

submit_method = """
  submitApplication: async (applicationNumber: string): Promise<ApplicationResponse> => {
    const response = await applicationApi.post<ApplicationResponse>(`/api/v1/applications/${applicationNumber}/submit`);
    return response.data;
  },
"""

if "submitApplication:" not in content:
    content = content.replace("grantConsent: async", submit_method + "\n  grantConsent: async")

with open('./frontend/src/services/serviceCatalog.ts', 'w') as f:
    f.write(content)
