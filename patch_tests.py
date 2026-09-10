with open('./frontend/src/__tests__/services.test.tsx', 'r') as f:
    content = f.read()

content = content.replace("submittedAt: new Date().toISOString(),", "submittedAt: new Date().toISOString(),\n        serviceName: 'Mock Service',\n        departmentCode: 'DEPT-MOCK',\n        createdAt: new Date().toISOString(),\n        updatedAt: null,")

with open('./frontend/src/__tests__/services.test.tsx', 'w') as f:
    f.write(content)
