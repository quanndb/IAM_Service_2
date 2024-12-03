package com.example.identityService.service.auth;

import com.example.identityService.DTO.request.AppLogoutRequest;
import com.example.identityService.DTO.request.LoginRequest;
import com.example.identityService.config.KeycloakProvider;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService extends AbstractAuthService{

    @Value("${keycloak.auth-server-url}")
    private String KEYCLOAK_AUTH_URL;
    @Value("${keycloak.credentials.client-id}")
    private String CLIENT_ID;
    @Value("${keycloak.credentials.secret}")
    private String CLIENT_SECRET;

    private final KeycloakProvider keycloakProvider;

    @Override
    public Object performLogin(LoginRequest request) {
        keycloakProvider.setKeycloak(request.getEmail(), request.getPassword());
        return keycloakProvider.getKeycloak()
                .tokenManager().getAccessToken();
    }

    @Override
    public boolean logout(AppLogoutRequest request) {
        String body = String.format("client_id=%s&client_secret=%s&refresh_token=%s",
                CLIENT_ID, CLIENT_SECRET, request.getRefreshToken());

        WebClient.create(KEYCLOAK_AUTH_URL)
                .post()
                .uri("/realms/IAM2/protocol/openid-connect/logout")
                .header("Content-Type", "application/x-www-form-urlencoded",
                                    "Authorization", "Bear " + request.getAccessToken())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Object.class).block();
        return true;
    }

    @Override
    public boolean performResetPassword(String email, String newPassword) {
        UsersResource usersResource = keycloakProvider.getRealmResourceWithAdminPrivilege().users();
        List<UserRepresentation> users = usersResource.search(email, true);
        if (!users.isEmpty()) {
            UserRepresentation user = users.getFirst();

            CredentialRepresentation credentials = new CredentialRepresentation();
            credentials.setTemporary(false);
            credentials.setType(CredentialRepresentation.PASSWORD);
            credentials.setValue(newPassword);

            usersResource.get(user.getId()).resetPassword(credentials);
        } else {
            throw new AppExceptions(ErrorCode.NOTFOUND_EMAIL);
        }
        return true;
    }

    @Override
    public Object getNewToken(String refreshToken) {
        String body = String.format("grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
                CLIENT_ID, CLIENT_SECRET, refreshToken);

        return WebClient.create(KEYCLOAK_AUTH_URL)
                .post()
                .uri("/realms/IAM2/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Object.class).block();
    }

    @Override
    public boolean performChangePassword(String email, String oldPassword, String newPassword) {
        return performResetPassword(email, newPassword);
    }
}
