package com.example.identityService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
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
@Table(name = "role")
@AllArgsConstructor
@RequiredArgsConstructor
public class Role extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private boolean deleted;
}
