package com.example.identityService.DTO.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GetKeycloakTokenRequest {
    private String grantType;
    private String clientId;
    private String clientSecret;
    private String refreshToken;
}
