package com.example.identityService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@Table(name = "permission")
@RequiredArgsConstructor
@AllArgsConstructor
public class Permission extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private boolean deleted;
}
