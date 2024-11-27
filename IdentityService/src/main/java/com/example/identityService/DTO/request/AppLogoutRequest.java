package com.example.identityService.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppLogoutRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String accessToken;
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String refreshToken;
}
