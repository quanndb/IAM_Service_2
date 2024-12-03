package com.example.identityService.DTO.request;

import com.example.identityService.DTO.EnumRole;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class AssignRoleRequest {
    @NotEmpty(message = "ROLE_NOT_EMPTY")
    List<EnumRole> roles;
}
