package com.example.identityService.entity;

import com.example.identityService.DTO.PermissionScope;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String roleId;
    @Column(nullable = false)
    private String permissionId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionScope scope;
    @Column(nullable = false)
    @CreatedBy
    private String createdBy;
    @Column(nullable = false)
    @CreatedDate
    private LocalDate createdDate;
}
