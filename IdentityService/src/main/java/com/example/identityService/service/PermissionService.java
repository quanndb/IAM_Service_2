package com.example.identityService.service;

import com.example.identityService.DTO.request.CreatePermissionRequest;
import com.example.identityService.entity.Permission;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.PermissionMapper;
import com.example.identityService.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @PreAuthorize("hasPermission('ROLES', 'CREATE')")
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

    @PreAuthorize("hasPermission('ROLES', 'UPDATE')")
    public boolean updatePermission(String roleId, CreatePermissionRequest request){
        Permission foundPermission = permissionRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.PERMISSION_NOTFOUND));
        permissionMapper.updatePermission(foundPermission, request);
        permissionRepository.save(foundPermission);
        return true;
    }

    @PreAuthorize("hasPermission('ROLES', 'DELETE')")
    public boolean deletePermission(String roleId){
        Permission foundPermission = permissionRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        foundPermission.setDeleted(true);
        permissionRepository.save(foundPermission);
        return true;
    }
}
