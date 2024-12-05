package com.example.identityService.DTO.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GetGoogleTokenRequest {
    private String code;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String grantType;
}
