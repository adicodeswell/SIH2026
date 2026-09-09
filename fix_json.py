def fix_json(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    old_json = """                                [
                                  {
                                    "citizenId": "%s",
                                    "fullName": "Test User",
                                    "aadhaarNumber": "123412341234"
                                  }
                                ]"""
    
    new_json = """                                [
                                  {
                                    "source": "EDUCATION_SYSTEM",
                                    "status": "SUCCESS",
                                    "data": {
                                      "citizenId": "%s",
                                      "fullName": "Test User",
                                      "aadhaarNumber": "123412341234"
                                    }
                                  }
                                ]"""
                                
    content = content.replace(old_json, new_json)
    
    with open(filepath, 'w') as f:
        f.write(content)

fix_json('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/workflow/ApplicationOrchestrationWorkflowTest.java')
fix_json('backend/security-workflow-service/src/test/java/com/mahasetu/securityworkflow/service/WorkflowIdempotencyTest.java')
