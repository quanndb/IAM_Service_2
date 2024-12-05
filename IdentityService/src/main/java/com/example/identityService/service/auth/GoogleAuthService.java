package com.example.identityService.service.auth;

import com.example.identityService.DTO.response.ExchangeTokenResponse;
import com.example.identityService.DTO.response.GoogleUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.oauth}")
    private String GOOGLE_OAUTH;

    @Value("${google.api-oauth}")
    private String GOOGLE_API_OAUTH;

    @Value("${google.client-id}")
    private String CLIENT_ID;

    @Value("${google.client-secret}")
    private String CLIENT_SECRET;

    @Value("${google.redirect-uri}")
    private String REDIRECT_URI;

    public ExchangeTokenResponse exchangeToken(String code){
        String body = String.format("code=%s&grant_type=authorization_code&client_id=%s&client_secret=%s&redirect_uri=%s",
                code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
        return WebClient.create()
                .post()
                .uri(GOOGLE_OAUTH+"/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ExchangeTokenResponse.class).block();
    }

    public GoogleUserResponse getUserInfo(String accessToken){
        return WebClient.create()
                .get()
                .uri(GOOGLE_API_OAUTH+"/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserResponse.class).block();
    }
//
//    public Object getNewToken(String refreshToken){
//        String body = String.format("grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
//                CLIENT_ID, CLIENT_SECRET, refreshToken);
//        return WebClient.create()
//                .post()
//                .uri(GOOGLE_OAUTH+"/token")
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(Object.class).block();
//    }
}
