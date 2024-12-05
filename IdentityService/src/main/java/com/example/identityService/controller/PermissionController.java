package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.request.CreatePermissionRequest;
import com.example.identityService.DTO.request.PageRequest;
import com.example.identityService.DTO.request.PermissionPageRequest;
import com.example.identityService.DTO.response.PageResponse;
import com.example.identityService.DTO.response.PermissionResponse;
import com.example.identityService.entity.Permission;
import com.example.identityService.service.PermissionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasPermission('ROLES', 'READ')")
    public ApiResponse<PageResponse<PermissionResponse>> getPermissions(@ModelAttribute PermissionPageRequest request) {
        return ApiResponse.<PageResponse<PermissionResponse>>builder()
                .code(200)
                .result(permissionService.getPermissions(request))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasPermission('ROLES', 'CREATE')")
    public ApiResponse<String> addPermission(@RequestBody @Valid CreatePermissionRequest request){
        boolean result = permissionService.createPermission(request);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{permissionId}")
    @PreAuthorize("hasPermission('ROLES', 'UPDATE')")
    public ApiResponse<String> updatePermission(@PathVariable String permissionId, @RequestBody @Valid CreatePermissionRequest request){
        boolean result = permissionService.updatePermission(permissionId, request);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasPermission('ROLES', 'DELETE')")
    public ApiResponse<String> deletePermission(@PathVariable String permissionId){
        boolean result = permissionService.deletePermission(permissionId);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }
}
