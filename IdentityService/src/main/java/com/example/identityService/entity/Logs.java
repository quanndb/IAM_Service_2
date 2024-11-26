package com.example.identityService.entity;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Logs {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @CreatedBy
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String ip;
    @Column(nullable = false)
    private String actionName;
    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime dateTime;
    private String note;
}
