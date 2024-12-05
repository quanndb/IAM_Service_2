package com.example.identityService.DTO.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExchangeTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
    private String scope;
    private String idToken;
    private String tokenType;
}
