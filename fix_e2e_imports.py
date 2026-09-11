import re

file = 'backend/application-service/src/test/java/com/mahasetu/application/controller/ApplicationE2EHttpTest.java'
with open(file, 'r') as f:
    content = f.read()

content = content.replace('com.github.tomakehurst.client.WireMock.', '')

with open(file, 'w') as f:
    f.write(content)

