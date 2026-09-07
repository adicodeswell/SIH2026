package com.mahasetu.interoperability.transformer;

import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;

@Component("EDUCATION_SYSTEM_TRANSFORMER")
public class EducationTransformer implements DataTransformer {

    @Override
    public CanonicalCitizenData transform(RawExternalResponse rawResponse) {
        try {
            String csvData = rawResponse.getRawData();
            String[] lines = csvData.split("\n");
            
            CanonicalCitizenData data = new CanonicalCitizenData();
            
            // Assuming Line 1 is header, Line 2 is data
            if (lines.length >= 2) {
                String[] values = lines[1].split(",");
                if (values.length >= 5) {
                    data.setCitizenId(values[0]);
                    data.setFullName(values[1]);
                    data.setHighestDegree(values[2]);
                    data.setUniversityName(values[3]);
                    data.setGraduationYear(Integer.parseInt(values[4].trim()));
                }
            }
            
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Failed to transform education CSV data", e);
        }
    }
}
