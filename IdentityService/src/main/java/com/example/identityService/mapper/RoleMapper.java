package com.example.identityService.mapper;

import com.example.identityService.DTO.request.CreateRoleRequest;
import com.example.identityService.DTO.request.RolePageRequest;
import com.example.identityService.DTO.response.RoleResponse;
import com.example.identityService.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRole(@MappingTarget Role response, CreateRoleRequest request);

    List<RoleResponse> toListRoleResponse(List<Role> res);
}
