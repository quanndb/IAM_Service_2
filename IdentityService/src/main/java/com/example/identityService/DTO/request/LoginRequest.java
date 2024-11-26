package com.example.identityService.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {
    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_PASSWORD_NOT_BLANK")
    private String email;
    @NotBlank(message = "EMAIL_PASSWORD_NOT_BLANK")
    private String password;
}
