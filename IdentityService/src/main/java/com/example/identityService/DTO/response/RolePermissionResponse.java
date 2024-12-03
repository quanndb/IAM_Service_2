package com.example.identityService.DTO.response;

import com.example.identityService.DTO.PermissionScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RolePermissionResponse {
    private String permissionName;
    private PermissionScope scope;
}
