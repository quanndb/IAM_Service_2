package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.AssignPermissionRequest;
import com.example.identityService.DTO.request.CreateRoleRequest;
import com.example.identityService.DTO.response.PageResponse;
import com.example.identityService.entity.Role;
import com.example.identityService.service.RolePermissionService;
import com.example.identityService.service.RoleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RolePermissionService rolePermissionService;
    private final RoleService roleService;

    @GetMapping
    public ApiResponse<PageResponse<Role>> getRoles(@RequestParam(required = false, defaultValue = "1") int page,
                                                    @RequestParam(required = false, defaultValue = "10") int size,
                                                    @RequestParam(required = false, defaultValue = "") String query,
                                                    @RequestParam(required = false, defaultValue = "id") String sortedBy,
                                                    @RequestParam(required = false, defaultValue = "DESC") EnumSortDirection sortDirection) throws JsonProcessingException {
        return ApiResponse.<PageResponse<Role>>builder()
                .code(200)
                .result(roleService.getRoles(page, size, query, sortedBy, sortDirection))
                .build();
    }

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
