package com.example.identityService.mapper;

import com.example.identityService.DTO.request.CreatePermissionRequest;
import com.example.identityService.DTO.response.PermissionResponse;
import com.example.identityService.entity.Permission;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePermission(@MappingTarget Permission response, CreatePermissionRequest request);

    List<PermissionResponse> toListPermissionResponse(List<Permission> res);
}
