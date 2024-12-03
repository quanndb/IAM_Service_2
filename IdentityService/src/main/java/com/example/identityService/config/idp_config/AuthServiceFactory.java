package com.example.identityService.config.idp_config;

import com.example.identityService.service.auth.AbstractAuthService;
import com.example.identityService.service.auth.DefaultAuthService;
import com.example.identityService.service.auth.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceFactory {

    private final DefaultAuthService defaultAuthService;
    private final KeycloakService keycloakAuthService;

    @Value("${app.idp}")
    private IdpProvider idpProvider;

    public AbstractAuthService getAuthService() {
        switch (idpProvider) {
            case KEYCLOAK:
                return keycloakAuthService;
            default:
                return defaultAuthService;
        }
    }
}
