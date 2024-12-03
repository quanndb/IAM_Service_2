package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.CreatePermissionRequest;
import com.example.identityService.DTO.response.PageResponse;
import com.example.identityService.entity.Permission;
import com.example.identityService.service.PermissionService;
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

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ApiResponse<PageResponse<Permission>> getPermissions(@RequestParam(required = false, defaultValue = "1") int page,
                                                                @RequestParam(required = false, defaultValue = "10") int size,
                                                                @RequestParam(required = false, defaultValue = "") String query,
                                                                @RequestParam(required = false, defaultValue = "id") String sortedBy,
                                                                @RequestParam(required = false, defaultValue = "DESC") EnumSortDirection sortDirection) throws JsonProcessingException {
        return ApiResponse.<PageResponse<Permission>>builder()
                .code(200)
                .result(permissionService.getPermissions(page, size, query, sortedBy, sortDirection))
                .build();
    }

    @PostMapping
    public ApiResponse<String> addPermission(@RequestBody @Valid CreatePermissionRequest request){
        boolean result = permissionService.createPermission(request);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{permissionId}")
    public ApiResponse<String> updatePermission(@PathVariable String permissionId, @RequestBody @Valid CreatePermissionRequest request){
        boolean result = permissionService.updatePermission(permissionId, request);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @DeleteMapping("/{permissionId}")
    public ApiResponse<String> deletePermission(@PathVariable String permissionId){
        boolean result = permissionService.deletePermission(permissionId);
        return ApiResponse.<String>builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }
}
