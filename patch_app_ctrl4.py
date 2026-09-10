import re

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'r') as f:
    content = f.read()

bad_chunk = """    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    
    @PostMapping("/{id}/submit")"""
    
fixed_chunk = """    @PostMapping("/{id}/submit")"""

content = content.replace(bad_chunk, fixed_chunk)

bad_chunk2 = """    }

    public ApplicationResponse updateApplicationStatus"""
    
fixed_chunk2 = """    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public ApplicationResponse updateApplicationStatus"""

content = content.replace(bad_chunk2, fixed_chunk2)

with open('./backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java', 'w') as f:
    f.write(content)
