import re

with open('frontend/src/__tests__/officer.test.tsx', 'r') as f:
    content = f.read()

content = content.replace('Source System Verified', 'Authoritative State System Verified')
content = content.replace('Claim Task', 'Claim File for Review')
content = content.replace('Submit Final Decision', 'Submit Statutory Resolution')

with open('frontend/src/__tests__/officer.test.tsx', 'w') as f:
    f.write(content)

