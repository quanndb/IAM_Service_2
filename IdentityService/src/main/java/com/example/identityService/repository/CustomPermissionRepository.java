package com.example.identityService.repository;

import com.example.identityService.DTO.request.PermissionPageRequest;
import com.example.identityService.DTO.response.PermissionResponse;

import java.util.List;

public interface CustomPermissionRepository {
    List<PermissionResponse> search(PermissionPageRequest request);
    Long count(PermissionPageRequest request);
}
