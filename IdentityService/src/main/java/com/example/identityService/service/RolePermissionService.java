package com.example.identityService.service;

import com.example.identityService.DTO.PermissionScope;
import com.example.identityService.entity.RolePermission;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.repository.PermissionRepository;
import com.example.identityService.repository.RolePermissionRepository;
import com.example.identityService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @PreAuthorize("hasPermission('accounts', 'CREATE')")
    public boolean assignPermission(String roleId, String permissionId, List<PermissionScope> scopes){
        roleRepository.findById(roleId).orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        permissionRepository.findById(roleId).orElseThrow(()-> new AppExceptions(ErrorCode.PERMISSION_NOTFOUND));
        for(PermissionScope item : scopes){
            boolean foundRolePermission = rolePermissionRepository
                    .existsByRoleIdAndPermissionIdAndScope(roleId, permissionId, item);
            if(!foundRolePermission){
                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .scope(item)
                        .build());
            }
        }

        return true;
    }

    // un assign
    @PreAuthorize("hasPermission('accounts', 'DELETE')")
    public boolean unAssignPermission(String roleId, String permissionId, List<PermissionScope> scopes) {
        List<RolePermission> rolePermission = rolePermissionRepository.findAllByRoleIdAndPermissionId(roleId, permissionId);
        List<RolePermission> deleteRolePermission = rolePermission.stream()
                .filter(item -> scopes.contains(item.getScope()))
                .toList();

        rolePermissionRepository.deleteAll(deleteRolePermission);
        return true;
    }
}
