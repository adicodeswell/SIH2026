import re

file_path = 'backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/VerificationServiceTest.java'
with open(file_path, 'r') as f:
    content = f.read()

# Replace SourceDataResult edu = new SourceDataResult("EDUCATION_SYSTEM", "SUCCESS", null, eduData);
# with SourceDataResult edu = new SourceDataResult(); edu.setSource("EDUCATION_SYSTEM"); edu.setStatus("SUCCESS"); edu.setData(eduData);

def replacer(match):
    var_name = match.group(1)
    source = match.group(2)
    status = match.group(3)
    error = match.group(4)
    data = match.group(5)
    
    res = f"SourceDataResult {var_name} = new SourceDataResult();\n"
    res += f"        {var_name}.setSource({source});\n"
    res += f"        {var_name}.setStatus({status});\n"
    if error != "null":
        res += f"        {var_name}.setError({error});\n"
    if data != "null":
        res += f"        {var_name}.setData({data});\n"
    return res

content = re.sub(r'SourceDataResult\s+(\w+)\s*=\s*new\s+SourceDataResult\(\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^)]+)\s*\);', replacer, content)

with open(file_path, 'w') as f:
    f.write(content)
