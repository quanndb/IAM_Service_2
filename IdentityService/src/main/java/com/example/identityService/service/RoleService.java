package com.example.identityService.service;

import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.CreateRoleRequest;
import com.example.identityService.DTO.request.RolePageRequest;
import com.example.identityService.DTO.response.PageResponse;
import com.example.identityService.DTO.response.RoleResponse;
import com.example.identityService.DTO.response.UserResponse;
import com.example.identityService.Util.JsonMapper;
import com.example.identityService.entity.Role;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.RoleMapper;
import com.example.identityService.repository.RolePermissionRepository;
import com.example.identityService.repository.RoleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final RolePermissionRepository rolePermissionRepository;
    private final JsonMapper jsonMapper;

    public List<String> getAllRolePermission(String roleId){
        return rolePermissionRepository.findAllByRoleIdAndDeletedIsFalse(roleId)
                .stream()
                .map(item -> item.getScope() + " " + item.getPermissionCode())
                .toList();
    }

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

    public boolean updateRole(String roleId, CreateRoleRequest request){
        Role foundRole = roleRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        roleMapper.updateRole(foundRole, request);
        roleRepository.save(foundRole);
        return true;
    }

    public boolean deleteRole(String roleId){
        Role foundRole = roleRepository.findById(roleId)
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        foundRole.setDeleted(true);
        roleRepository.save(foundRole);
        return true;
    }

    public PageResponse<RoleResponse> getRoles(RolePageRequest request) {
        long totalRecords = roleRepository.count(request);
        List<RoleResponse> roleResponses = roleRepository.search(request);
        return PageResponse.<RoleResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .query(request.getQuery())
                .sortedBy(request.getSortedBy())
                .sortDirection(request.getSortDirection().name())
                .first(request.getPage() == 1)
                .last(request.getPage() % request.getSize() == request.getPage())
                .totalRecords(totalRecords)
                .totalPages(request.getPage() % request.getSize())
                .response(roleResponses)
                .build();
    }
}
