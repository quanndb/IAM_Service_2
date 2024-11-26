package com.example.identityService.DTO.request;

import com.example.identityService.DTO.PermissionScope;
import lombok.Getter;

import java.util.List;

@Getter
public class AssignPermissionRequest {
    List<PermissionScope> scopes;
}
