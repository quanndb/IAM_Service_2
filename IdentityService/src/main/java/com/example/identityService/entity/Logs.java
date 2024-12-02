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
@Table(name = "logs")
@RequiredArgsConstructor
@AllArgsConstructor
public class Logs extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String ip;
    @Column(nullable = false)
    private String actionName;
    @Column(nullable = false)
    private String note;
}
