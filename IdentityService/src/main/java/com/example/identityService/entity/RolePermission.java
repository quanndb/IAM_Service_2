package com.example.identityService.entity;

import com.example.identityService.DTO.PermissionScope;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@Table(name = "role_permission")
@RequiredArgsConstructor
@AllArgsConstructor
public class RolePermission extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String roleId;
    @Column(nullable = false)
    private String permissionCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionScope scope;
    @Column(nullable = false)
    private boolean deleted;
}
