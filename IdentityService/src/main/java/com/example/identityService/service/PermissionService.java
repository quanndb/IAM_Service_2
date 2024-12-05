package com.example.identityService.service;

import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.CreatePermissionRequest;
import com.example.identityService.DTO.request.PermissionPageRequest;
import com.example.identityService.DTO.response.PageResponse;
import com.example.identityService.DTO.response.PermissionResponse;
import com.example.identityService.DTO.response.UserResponse;
import com.example.identityService.Util.JsonMapper;
import com.example.identityService.entity.Permission;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.PermissionMapper;
import com.example.identityService.repository.PermissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final JsonMapper jsonMapper;

    public boolean createPermission(CreatePermissionRequest request){
        permissionRepository.findByCodeIgnoreCase(request.getCode())
                        .ifPresent(_ -> {
                            throw new AppExceptions(ErrorCode.PERMISSION_EXISTED);
                        });
        permissionRepository.save(Permission.builder()
                .name(request.getName())
                .code(request.getCode())
                .build());
        return true;
    }

    public boolean updatePermission(String roleId, CreatePermissionRequest request){
        Permission foundPermission = permissionRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.PERMISSION_NOTFOUND));
        permissionMapper.updatePermission(foundPermission, request);
        permissionRepository.save(foundPermission);
        return true;
    }

    public boolean deletePermission(String roleId){
        Permission foundPermission = permissionRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        foundPermission.setDeleted(true);
        permissionRepository.save(foundPermission);
        return true;
    }

    public PageResponse<PermissionResponse> getPermissions(PermissionPageRequest request) {
        long totalRecords = permissionRepository.count(request);
        List<PermissionResponse> permissionResponses = permissionRepository.search(request);
        return PageResponse.<PermissionResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .query(request.getQuery())
                .sortedBy(request.getSortedBy())
                .sortDirection(request.getSortDirection().name())
                .first(request.getPage() == 1)
                .last(request.getPage() % request.getSize() == request.getPage())
                .totalRecords(totalRecords)
                .totalPages(request.getPage() % request.getSize())
                .response(permissionResponses)
                .build();
    }
}
