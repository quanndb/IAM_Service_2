package com.example.identityService.service.auth;

import com.example.identityService.DTO.request.AppLogoutRequest;
import com.example.identityService.DTO.request.DefaultLoginRequest;
import com.example.identityService.DTO.request.RegisterRequest;
import com.example.identityService.DTO.response.UserResponse;

public interface IAuthService{
    Object login(DefaultLoginRequest request);
    boolean logout(AppLogoutRequest request);
    boolean register(RegisterRequest request);
    Object getProfile(String token);
    String getNewToken(String refreshToken);
}

