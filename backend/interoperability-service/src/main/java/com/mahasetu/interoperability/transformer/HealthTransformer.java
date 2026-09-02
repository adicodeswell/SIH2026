package com.mahasetu.interoperability.transformer;

import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("HEALTH_SYSTEM_TRANSFORMER")
public class HealthTransformer implements DataTransformer {

    @Override
    public CanonicalCitizenData transform(RawExternalResponse rawResponse) {
        try {
            String xmlData = rawResponse.getRawData();
            CanonicalCitizenData data = new CanonicalCitizenData();
            
            // Very simple XML parsing for MVP (Regex)
            data.setCitizenId(extractTag(xmlData, "CitizenID"));
            data.setFullName(extractTag(xmlData, "Name"));
            
            // Health specific data could be added to CanonicalCitizenData in the future.
            
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Failed to transform health XML data", e);
        }
    }
    
    private String extractTag(String xml, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">");
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
