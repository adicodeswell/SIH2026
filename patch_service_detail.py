import re

with open('./frontend/src/pages/citizen/ServiceDetailPage.tsx', 'r') as f:
    content = f.read()

# Modify the mutationFn
old_mutation = """    mutationFn: async (payload: { citizenId: string; serviceCode: string }) => {
      // 1. Grant cryptographic consent first under DPDP
      await serviceCatalogApi.grantConsent({
        dataScope:
          payload.serviceCode === 'SCHOLARSHIP'
            ? 'education'
            : payload.serviceCode === 'SRV-EDU'
            ? 'education,health'
            : 'education,employment,skills',
        purpose:
          payload.serviceCode === 'SCHOLARSHIP' ? 'scholarship_verification' : 'verification',
        requestingDepartmentId: service?.departmentCode || 'DEPT_EMP',
      });

      // 2. Submit application to application-service
      return await serviceCatalogApi.createApplication({
        citizenId: payload.citizenId,
        serviceCode: payload.serviceCode,
      });
    },"""

new_mutation = """    mutationFn: async (payload: { citizenId: string; serviceCode: string }) => {
      // 1. Create application in DRAFT state
      const application = await serviceCatalogApi.createApplication({
        citizenId: payload.citizenId,
        serviceCode: payload.serviceCode,
      });

      // 2. Grant cryptographic consent bound to the application
      await serviceCatalogApi.grantConsent({
        applicationId: application.applicationNumber,
        serviceCode: payload.serviceCode,
        dataScope:
          payload.serviceCode === 'SCHOLARSHIP'
            ? 'education'
            : payload.serviceCode === 'SRV-EDU'
            ? 'education,health'
            : 'education,employment,skills',
        purpose:
          payload.serviceCode === 'SCHOLARSHIP' ? 'scholarship_verification' : 'verification',
        requestingDepartmentId: service?.departmentCode || 'DEPT_EMP',
      });

      // 3. Submit/activate application to start workflow
      return await serviceCatalogApi.submitApplication(application.applicationNumber);
    },"""

content = content.replace(old_mutation, new_mutation)

with open('./frontend/src/pages/citizen/ServiceDetailPage.tsx', 'w') as f:
    f.write(content)
