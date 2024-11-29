package com.example.identityService.service.auth;

import com.example.identityService.DTO.request.*;
import com.example.identityService.config.KeycloakProvider;
import com.example.identityService.config.properties.KeycloakCredentialsProperties;
import com.example.identityService.entity.Account;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class KeycloakService implements IAuthService{

    @Value("${keycloak.auth-server-url}")
    private String KEYCLOAK_AUTH_URL;
    @Value("${keycloak.realm}")
    private String REALM;
    @Value("${keycloak.credentials.client-id}")
    private String CLIENT_ID;
    @Value("${keycloak.credentials.secret}")
    private String CLIENT_SECRET;
    @Value("${keycloak.credentials.username}")
    private String ADMIN;
    @Value("${keycloak.credentials.password}")
    private String PASSWORD;

    private final AccountRepository accountRepository;
    private final DefaultAuthService defaultAuthService;
    private final KeycloakProvider keycloakProvider;
    private final KeycloakCredentialsProperties properties;
    private final WebClient.Builder webClientBuilder;

    @Override
    public Object login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        if(!account.isEnable()) throw new AppExceptions(ErrorCode.ACCOUNT_LOCKED);
        if(account.isDeleted()) throw new AppExceptions(ErrorCode.ACCOUNT_DELETED);
        if(!account.isVerified()) throw new AppExceptions(ErrorCode.NOT_VERIFY_ACCOUNT);
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

        keycloakProvider.getRealmResourceWithAdminPrivilege().users().create(user);
        keycloakProvider.getKeycloak().close();
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
}
