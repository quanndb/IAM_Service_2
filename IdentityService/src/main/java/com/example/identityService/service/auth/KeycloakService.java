package com.example.identityService.service.auth;

import com.example.identityService.DTO.request.*;
import com.example.identityService.config.KeycloakProvider;
import com.example.identityService.config.properties.KeycloakCredentialsProperties;
import com.example.identityService.repository.keyCloakRepositories.AuthClient;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService implements IAuthService{

    private final AuthClient authClient;
    private final DefaultAuthService defaultAuthService;
    private final KeycloakCredentialsProperties properties;
    private final KeycloakProvider keycloakProvider;

    @Override
    public Object login(LoginRequest request) {

        return authClient.loginWithKeycloak(Map.of(
                "username", request.getEmail(),
                "password", request.getPassword(),
                "client_id", properties.getClientId(),
                "client_secret", properties.getSecret(),
                "scope", properties.getScope(),
                "grant_type", properties.getGrantType()
        ));
    }

    @Override
    public boolean logout(AppLogoutRequest request) {
        return false;
    }

    @Override
    public boolean register(RegisterRequest request) {
        defaultAuthService.register(request);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFullname().split(" ")[0]);
        user.setLastName(request.getFullname().split(" ")[1]);
        user.setEnabled(true);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(request.getPassword());
        passwordCred.setTemporary(false);

        user.setCredentials(Collections.singletonList(passwordCred));

        keycloakProvider.getRealmResource().users().create(user);
        keycloakProvider.getKeycloak().close();
        return true;
    }

    @Override
    public Object getProfile(String token) {
        return authClient.getUserInfo(token);
    }

    @Override
    public Object getNewToken(String refreshToken) {
        return authClient.getNewToken(Map.of(
                "grant_type", "refresh_token",
                "client_id", "iam-client",
                "client_secret", "vUAlBnG43sreZsAr7hvdqOz5S9FYz0Il",
                "refresh_token", refreshToken));
    }
}
