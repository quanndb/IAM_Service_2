package com.example.identityService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@Setter
@Builder
@Table(name = "account_role")
@RequiredArgsConstructor
@AllArgsConstructor
public class AccountRole extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String accountId;
    @Column(nullable = false)
    private String roleId;
    @Column(nullable = false)
    private boolean deleted;
}
