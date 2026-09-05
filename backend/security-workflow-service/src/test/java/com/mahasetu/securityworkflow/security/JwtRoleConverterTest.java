package com.mahasetu.securityworkflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtRoleConverterTest {

    private final JwtRoleConverter converter = new JwtRoleConverter();

    @Test
    void testConvert_ExtractsRoles() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(Map.of("roles", List.of("citizen", "officer")));

        AbstractAuthenticationToken token = converter.convert(jwt);
        Collection<GrantedAuthority> authorities = token.getAuthorities();

        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        assertTrue(authorityNames.contains("ROLE_CITIZEN"));
        assertTrue(authorityNames.contains("ROLE_OFFICER"));
    }
}
