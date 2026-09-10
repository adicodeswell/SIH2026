package com.mahasetu.interoperability.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;

@Component("EMPLOYMENT_SYSTEM_TRANSFORMER")
public class EmploymentTransformer implements DataTransformer {

    private final ObjectMapper objectMapper;

    public EmploymentTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CanonicalCitizenData transform(RawExternalResponse rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse.getRawData());
            CanonicalCitizenData data = new CanonicalCitizenData();
            
            // Map legacy fields to canonical model
            if (root.has("cit_id")) data.setCitizenId(root.get("cit_id").asText());
            if (root.has("full_name")) data.setFullName(root.get("full_name").asText());
            if (root.has("dob")) data.setDateOfBirth(root.get("dob").asText());
            if (root.has("emp_status")) data.setEmploymentStatus(root.get("emp_status").asText());
            if (root.has("highest_degree")) data.setHighestDegree(root.get("highest_degree").asText());
            if (root.has("grad_year")) data.setGraduationYear(root.get("grad_year").asInt());
            
            if (root.has("annual_family_income")) {
                data.setAnnualFamilyIncome(new java.math.BigDecimal(root.get("annual_family_income").asText()));
            }
            if (root.has("category")) {
                data.setCategory(root.get("category").asText());
            }
            if (root.has("bank_account")) {
                data.setBankAccountNumber(root.get("bank_account").asText());
            }
            
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Failed to transform employment data", e);
        }
    }
}
