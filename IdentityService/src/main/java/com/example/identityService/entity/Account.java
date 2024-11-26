package com.example.identityService.entity;

import com.example.identityService.DTO.Gender;
import jakarta.persistence.*;
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
public class Account {
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
    private boolean isVerified;
    @Column(nullable = false)
    private String roleId;
    private Gender gender;
    private String address;
    private String cloudImageId;
    private String cloudImageUrl;
}
