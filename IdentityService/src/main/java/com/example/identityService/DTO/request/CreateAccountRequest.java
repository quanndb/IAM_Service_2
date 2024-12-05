package com.example.identityService.DTO.request;

import com.example.identityService.DTO.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateAccountRequest {
    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_PASSWORD_NOT_BLANK")
    private String email;
    @NotBlank(message = "FIELD_NOT_BLANK")
    @Size(min = 8, message = "PASSWORD_AT_LEAST")
    private String password;
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String fullname;
    private Boolean verified;
    private Boolean enable;
    @NotEmpty(message = "ROLE_NOT_EMPTY")
    private List<String> roles;
    private Gender gender;
    private String address;
    private String ip;
}
