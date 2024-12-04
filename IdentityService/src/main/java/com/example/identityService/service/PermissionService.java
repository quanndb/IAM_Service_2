package com.example.identityService.service;

import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.CreatePermissionRequest;
import com.example.identityService.DTO.response.PageResponse;
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

    public PageResponse<Permission> getPermissions(int page, int size, String query, String sortedBy, EnumSortDirection sortDirection) throws JsonProcessingException {
        var res = permissionRepository.getPermissionData(page, size, query, sortedBy, sortDirection.name());
        int totalRecords = (int) res.getFirst()[0];
        String permissionJson = (String) res.getFirst()[1];
        List<Permission> permissionList = jsonMapper
                .JSONToList(permissionJson == null? "[]" : permissionJson, Permission.class);
        return PageResponse.<Permission>builder()
                .page(page)
                .size(size)
                .query(query)
                .sortedBy(sortedBy)
                .sortDirection(sortDirection.name())
                .first(page == 1)
                .last(page % size == page)
                .totalRecords(totalRecords)
                .totalPages(page % size)
                .response(permissionList)
                .build();
    }
}
