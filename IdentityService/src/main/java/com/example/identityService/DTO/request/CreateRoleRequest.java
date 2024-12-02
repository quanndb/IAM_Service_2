package com.example.identityService.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateRoleRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String name;
    private String description;
}
