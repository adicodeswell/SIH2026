package com.mahasetu.interoperability.connector;

import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.springframework.stereotype.Component;

@Component("SKILLS_SYSTEM_CONNECTOR")
public class SkillsConnector implements GovernmentSystemConnector {

    @Override
    public RawExternalResponse fetch(String citizenId, ExternalSystem system) {
        // Mocking a successful response for the skills system
        String rawJson = "{\"citizenId\": \"" + citizenId + "\", \"skillStatus\": \"CERTIFIED\"}";
        return new RawExternalResponse(rawJson, system);
    }
}
