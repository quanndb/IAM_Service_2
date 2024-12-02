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
    String id;
    @Column(nullable = false)
    String accountId;
    @Column(nullable = false)
    String roleId;
}
