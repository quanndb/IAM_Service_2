package com.example.identityService.DTO.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class AssignRoleRequest {
    @NotEmpty(message = "ROLE_NOT_EMPTY")
    List<String> roles;
}
