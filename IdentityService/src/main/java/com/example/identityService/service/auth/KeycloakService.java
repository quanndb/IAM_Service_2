package com.example.identityService.service.auth;

import com.example.identityService.DTO.request.AppLogoutRequest;
import com.example.identityService.DTO.request.DefaultLoginRequest;
import com.example.identityService.DTO.request.RegisterRequest;
import com.example.identityService.DTO.response.LoginResponse;
import com.example.identityService.repository.keyCloakRepositories.ProfileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakService implements IAuthService{

    private final ProfileClient profileClient;

    @Override
    public String login(DefaultLoginRequest request) {
        return "http://localhost:8081/auth/realms/IAM2/protocol/openid-connect/auth";
    }

    @Override
    public boolean logout(AppLogoutRequest request) {
        return false;
    }

    @Override
    public boolean register(RegisterRequest request) {
        return false;
    }

    @Override
    public Object getProfile(String token) {
        return profileClient.getUserInfo(token);
    }

    @Override
    public String getNewToken(String refreshToken) {
        return "";
    }
}
