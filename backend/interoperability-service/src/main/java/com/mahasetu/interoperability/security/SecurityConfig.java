package com.mahasetu.interoperability.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                // Actuator can be public for health checks
                .requestMatchers("/actuator/**").permitAll()
                // Scoped fetch endpoint only allowed for security-workflow-service
                .requestMatchers("/api/v1/interop/fetch/scoped").hasAuthority("SCOPE_interop:fetch:scoped")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String clientId = jwt.getClaimAsString("clientId");
            if (clientId == null) {
                clientId = jwt.getClaimAsString("azp"); // Keycloak typically puts it in azp for service accounts
            }
            if ("security-workflow-service".equals(clientId)) {
                return Collections.singletonList(new SimpleGrantedAuthority("SCOPE_interop:fetch:scoped"));
            }
            return Collections.emptyList();
        });
        return converter;
    }
}
