package com.example.identityService.service.auth;

import com.example.identityService.DTO.request.AppLogoutRequest;
import com.example.identityService.DTO.request.LoginRequest;
import com.example.identityService.DTO.request.RegisterRequest;

public interface IAuthService{
    Object login(LoginRequest request);
    boolean logout(AppLogoutRequest request);
    boolean register(RegisterRequest request);
    Object getProfile(String token);
    Object getNewToken(String refreshToken);
}

