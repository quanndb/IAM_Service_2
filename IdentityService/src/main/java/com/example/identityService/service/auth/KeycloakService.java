package com.example.identityService.service.auth;

import com.example.identityService.DTO.request.ChangePasswordRequest;
import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.DTO.request.LoginRequest;
import com.example.identityService.DTO.request.RegisterRequest;
import com.example.identityService.DTO.response.LoginResponse;
import com.example.identityService.config.KeycloakProvider;
import com.example.identityService.entity.Account;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.AccountMapper;
import com.example.identityService.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
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
    private final AccountMapper accountMapper;
    private final TokenService tokenService;

    @Override
    public LoginResponse performLogin(LoginRequest request) {
        keycloakProvider.setKeycloak(request.getEmail(), request.getPassword());
        AccessTokenResponse response = keycloakProvider.getKeycloak()
                .tokenManager().getAccessToken();
        return new LoginResponse(response.getToken(), response.getRefreshToken());
    }

    @Override
    public LoginResponse performLoginWithGoogle(String email, String password, String ip) {
        return performLogin(new LoginRequest(email, password, ip));
    }

    @Override
    public boolean performRegister(RegisterRequest request) {
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
    public boolean performCreateUser(CreateAccountRequest request) {
        return performRegister(accountMapper.toRegisterRequest(request));
    }

    @Override
    public boolean performRegisterUserFromGoogle(Account request, String ip) {
        performRegister(RegisterRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullname(request.getFullname())
                .ip(ip)
                .build());
        return true;
    }

    @Override
    public boolean logout(String accessToken, String refreshToken) {
        String body = String.format("client_id=%s&client_secret=%s&refresh_token=%s",
                CLIENT_ID, CLIENT_SECRET, refreshToken);

        WebClient.create(KEYCLOAK_AUTH_URL)
                .post()
                .uri("/realms/IAM2/protocol/openid-connect/logout")
                .header("Content-Type", "application/x-www-form-urlencoded",
                                    "Authorization", "Bear " + accessToken)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Object.class).block();
        return true;
    }

    @Override
    public boolean performResetPassword(String token, String newPassword, String ip) {
        String email = tokenService.getTokenDecoded(token).getSubject();

        return changePassword(email, newPassword);
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
    public boolean performChangePassword(ChangePasswordRequest request, String ip) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return changePassword(email, request.getNewPassword());
    }

    public boolean changePassword(String email, String newPassword){
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
}
