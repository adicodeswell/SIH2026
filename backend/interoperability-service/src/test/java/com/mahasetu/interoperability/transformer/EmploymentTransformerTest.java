package com.mahasetu.interoperability.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmploymentTransformerTest {

    @Test
    public void testTransform() {
        EmploymentTransformer transformer = new EmploymentTransformer(new ObjectMapper());
        String mockJson = "{\"cit_id\":\"MH1001\",\"full_name\":\"Test User\",\"dob\":\"2000-01-01\",\"emp_status\":\"EMPLOYED\",\"highest_degree\":\"B.Tech\",\"grad_year\":2022}";
        RawExternalResponse response = new RawExternalResponse(mockJson, ExternalSystem.EMPLOYMENT_SYSTEM);
        
        CanonicalCitizenData result = transformer.transform(response);
        
        assertEquals("MH1001", result.getCitizenId());
        assertEquals("Test User", result.getFullName());
        assertEquals("EMPLOYED", result.getEmploymentStatus());
    }
}
