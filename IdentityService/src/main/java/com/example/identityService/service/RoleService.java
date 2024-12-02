package com.example.identityService.service;

import com.example.identityService.DTO.request.CreateRoleRequest;
import com.example.identityService.entity.Role;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.RoleMapper;
import com.example.identityService.repository.RolePermissionRepository;
import com.example.identityService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final RolePermissionRepository rolePermissionRepository;

    @PreAuthorize("hasPermission('ROLES', 'READ')")
    public List<String> getAllRolePermission(String roleId){
        return rolePermissionRepository.findAllByRoleId(roleId)
                .stream()
                .map(item -> item.getScope() + " " + item.getPermissionCode())
                .toList();
    }

    @PreAuthorize("hasPermission('ROLES', 'CREATE')")
    public boolean createRole(CreateRoleRequest request){
        roleRepository.findByNameIgnoreCase(request.getName())
                        .ifPresent( _ -> {
                            throw new AppExceptions(ErrorCode.ROLE_EXISTED);
                        });
        roleRepository.save(Role.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                .build());
        return true;
    }

    @PreAuthorize("hasPermission('ROLES', 'UPDATE')")
    public boolean updateRole(String roleId, CreateRoleRequest request){
        Role foundRole = roleRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        roleMapper.updateRole(foundRole, request);
        roleRepository.save(foundRole);
        return true;
    }

    @PreAuthorize("hasPermission('ROLES', 'DELETE')")
    public boolean deleteRole(String roleId){
        Role foundRole = roleRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        foundRole.setDeleted(true);
        roleRepository.save(foundRole);
        return true;
    }
}
