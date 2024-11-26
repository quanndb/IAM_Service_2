package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.request.AssignPermissionRequest;
import com.example.identityService.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RolePermissionService rolePermissionService;

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ApiResponse<String> assignPermissionForRole(@PathVariable String roleId,
                                                       @PathVariable String permissionId,
                                                       @RequestBody AssignPermissionRequest assignPermissionRequest){

        boolean result = rolePermissionService.assignPermission(roleId,
                permissionId, assignPermissionRequest.getScopes());
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }
}
