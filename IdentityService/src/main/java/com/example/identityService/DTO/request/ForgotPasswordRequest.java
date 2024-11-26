package com.example.identityService.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ForgotPasswordRequest {
    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_PASSWORD_NOT_BLANK")
    private String email;
}
