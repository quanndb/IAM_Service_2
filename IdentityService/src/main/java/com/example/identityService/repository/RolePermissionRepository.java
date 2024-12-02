package com.example.identityService.repository;

import com.example.identityService.DTO.PermissionScope;
import com.example.identityService.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    List<RolePermission> findAllByRoleIdAndPermissionCodeIgnoreCase(String roleId, String permissionCode);
    List<RolePermission> findAllByRoleId(String roleId);

    boolean existsByRoleIdAndPermissionCodeIgnoreCaseAndScope(String roleId, String permissionId, PermissionScope scope);
}
