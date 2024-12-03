package com.example.identityService.service;

import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.CreateRoleRequest;
import com.example.identityService.DTO.response.PageResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final RolePermissionRepository rolePermissionRepository;
    private final JsonMapper jsonMapper;

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

    @PreAuthorize("hasPermission('ROLES', 'READ')")
    public PageResponse<Role> getRoles(int page, int size, String query, String sortedBy, EnumSortDirection sortDirection) throws JsonProcessingException {
            var res = roleRepository.getRoleData(page, size, query, sortedBy, sortDirection.name());
            int totalRecords = (int) res.getFirst()[0];
            String roleJson = (String) res.getFirst()[1];
            List<Role> roleList = jsonMapper
                    .JSONToList(roleJson == null? "[]" : roleJson, Role.class);
            return PageResponse.<Role>builder()
                    .page(page)
                    .size(size)
                    .query(query)
                    .sortedBy(sortedBy)
                    .sortDirection(sortDirection.name())
                    .isFirst(page == 1)
                    .isLast(page % size == page)
                    .totalRecords(totalRecords)
                    .totalPages(page % size)
                    .response(roleList)
                    .build();
    }
}
