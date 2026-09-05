package com.mahasetu.interoperability.transformer;

import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EducationTransformerTest {

    @Test
    public void testTransformCsv() {
        EducationTransformer transformer = new EducationTransformer();
        String mockCsv = "ID,STUDENT_NAME,DEGREE,UNIVERSITY,YEAR\nMH3003,Priya Singh,B.Sc,Pune University,2024\n";
               
        RawExternalResponse response = new RawExternalResponse(mockCsv, ExternalSystem.EDUCATION_SYSTEM);
        CanonicalCitizenData result = transformer.transform(response);
        
        assertEquals("MH3003", result.getCitizenId());
        assertEquals("Priya Singh", result.getFullName());
        assertEquals("B.Sc", result.getHighestDegree());
        assertEquals(2024, result.getGraduationYear());
    }
}
