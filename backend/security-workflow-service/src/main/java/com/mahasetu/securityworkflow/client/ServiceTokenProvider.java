package com.mahasetu.securityworkflow.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class ServiceTokenProvider {

    private final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager;
    private final String registrationId;

    public ServiceTokenProvider(ClientRegistrationRepository clientRegistrationRepository,
                                OAuth2AuthorizedClientService authorizedClientService,
                                @Value("${mahasetu.application-service.registration-id:security-workflow-service}") String registrationId) {
        this.authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
        );
        this.registrationId = registrationId;
    }

    public String getAuthorizationHeader() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(registrationId)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("Unable to obtain service access token");
        }

        return "Bearer " + authorizedClient.getAccessToken().getTokenValue();
    }
}
