package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.request.AssignPermissionRequest;
import com.example.identityService.DTO.request.CreateRoleRequest;
import com.example.identityService.service.RolePermissionService;
import com.example.identityService.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RolePermissionService rolePermissionService;
    private final RoleService roleService;

    @GetMapping("{roleId}")
    public ApiResponse<List<String>> getAllPermissionOfRole(@PathVariable String roleId){
        return ApiResponse.<List<String>>builder()
                .code(200)
                .result(roleService.getAllRolePermission(roleId))
                .build();
    }

    @PostMapping
    public ApiResponse<String> addRole(@RequestBody @Valid CreateRoleRequest request){
        boolean result = roleService.createRole(request);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{roleId}")
    public ApiResponse<String> updateRole(@PathVariable String roleId, @RequestBody @Valid CreateRoleRequest request){
        boolean result = roleService.updateRole(roleId, request);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @DeleteMapping("/{roleId}")
    public ApiResponse<String> deleteRole(@PathVariable String roleId){
        boolean result = roleService.deleteRole(roleId);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{roleId}/permissions/{permissionCode}")
    public ApiResponse<String> assignPermissionsForRole(@PathVariable String roleId,
                                                       @PathVariable String permissionCode,
                                                       @RequestBody AssignPermissionRequest assignPermissionRequest){

        boolean result = rolePermissionService.assignPermission(roleId,
                permissionCode, assignPermissionRequest.getScopes());
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionCode}")
    public ApiResponse<String> unassignPermissionsForRole(@PathVariable String roleId,
                                                       @PathVariable String permissionCode,
                                                       @RequestBody AssignPermissionRequest assignPermissionRequest){

        boolean result = rolePermissionService.unAssignPermission(roleId,
                permissionCode, assignPermissionRequest.getScopes());
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }
}
