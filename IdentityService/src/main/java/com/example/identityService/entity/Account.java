package com.example.identityService.entity;

import com.example.identityService.DTO.Gender;
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
@Table(name = "account")
@RequiredArgsConstructor
@AllArgsConstructor
public class Account extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String fullname;
    @Column(nullable = false)
    private boolean verified;
    @Column(nullable = false)
    private boolean enable;
    @Column(nullable = false)
    private boolean deleted;
    private Gender gender;
    private String address;
    private String cloudImageId;
    private String cloudImageUrl;
}
