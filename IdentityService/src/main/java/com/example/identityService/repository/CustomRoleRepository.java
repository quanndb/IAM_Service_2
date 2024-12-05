package com.example.identityService.repository;

import com.example.identityService.DTO.request.RolePageRequest;
import com.example.identityService.DTO.response.RoleResponse;

import java.util.List;

public interface CustomRoleRepository {
    List<RoleResponse> search(RolePageRequest request);
    Long count(RolePageRequest request);
}
