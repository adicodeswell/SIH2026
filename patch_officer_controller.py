with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/controller/OfficerReviewController.java', 'r') as f:
    content = f.read()

old_code = """        String department = jwtAuth.getToken().getClaimAsString("department");

        if (department == null || department.trim().isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Officer department not found");
        }

        return department.trim().toUpperCase(java.util.Locale.ROOT);"""

new_code = """        Object deptClaim = jwtAuth.getToken().getClaim("department");
        String department = null;
        if (deptClaim instanceof java.util.Collection collection) {
            if (!collection.isEmpty()) {
                department = String.valueOf(collection.iterator().next());
            }
        } else if (deptClaim instanceof String str) {
            department = str;
        } else if (deptClaim != null) {
            department = String.valueOf(deptClaim);
        }

        if (department == null || department.trim().isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Officer department not found");
        }

        // Clean up brackets if Keycloak serialized array as string e.g. "[\"DEPT-SKILLS\"]"
        department = department.replaceAll("^\\\\[\\\"?|\\\"?\\\\]$", "");
        
        return department.trim().toUpperCase(java.util.Locale.ROOT);"""

content = content.replace(old_code, new_code)

with open('./backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/controller/OfficerReviewController.java', 'w') as f:
    f.write(content)
