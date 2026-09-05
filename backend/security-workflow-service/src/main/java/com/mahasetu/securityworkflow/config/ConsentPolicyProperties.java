package com.mahasetu.securityworkflow.config;

import com.mahasetu.securityworkflow.dto.ConsentPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for consent policies mapped by service code.
 * Binds to YAML under mahasetu.consent-policy.policies.
 *
 * Example:
 *   mahasetu:
 *     consent-policy:
 *       policies:
 *         SKILL_BENEFIT:
 *           dataScope: "education,employment,skills"
 *           purpose: "verification"
 */
@Configuration
@ConfigurationProperties(prefix = "mahasetu.consent-policy")
public class ConsentPolicyProperties {

    private Map<String, ConsentPolicy> policies = new HashMap<>();

    public Map<String, ConsentPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<String, ConsentPolicy> policies) {
        this.policies = policies;
    }
}
