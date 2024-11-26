package com.example.identityService.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LogoutRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String refreshToken;
}
