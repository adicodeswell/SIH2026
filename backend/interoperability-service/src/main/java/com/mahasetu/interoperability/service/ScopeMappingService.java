package com.mahasetu.interoperability.service;

import com.mahasetu.interoperability.model.DataScope;
import com.mahasetu.interoperability.model.ExternalSystem;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ScopeMappingService {
    private final Map<DataScope, ExternalSystem> mapping = new EnumMap<>(DataScope.class);

    public ScopeMappingService() {
        mapping.put(DataScope.EDUCATION, ExternalSystem.EDUCATION_SYSTEM);
        mapping.put(DataScope.EMPLOYMENT, ExternalSystem.EMPLOYMENT_SYSTEM);
        mapping.put(DataScope.HEALTH, ExternalSystem.HEALTH_SYSTEM);
        mapping.put(DataScope.SKILLS, ExternalSystem.SKILLS_SYSTEM);
    }

    public Optional<ExternalSystem> getSystemForScope(DataScope scope) {
        return Optional.ofNullable(mapping.get(scope));
    }
}
