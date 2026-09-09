package com.mahasetu.interoperability.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;

@Component("SKILLS_SYSTEM_TRANSFORMER")
public class SkillsTransformer implements DataTransformer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CanonicalCitizenData transform(RawExternalResponse rawResponse) {
        CanonicalCitizenData canonicalData = new CanonicalCitizenData();

        try {
            JsonNode rootNode = objectMapper.readTree((String) rawResponse.getRawData());
            
            if (rootNode.has("citizenId")) {
                canonicalData.setCitizenId(rootNode.get("citizenId").asText());
            }
            if (rootNode.has("skillStatus")) {
                canonicalData.setSkillStatus(rootNode.get("skillStatus").asText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return canonicalData;
    }
}
