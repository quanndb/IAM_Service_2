package com.example.identityService.DTO.response;

import com.example.identityService.DTO.Gender;
import com.example.identityService.entity.RolePermission;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String email;
    private String fullname;
    private String address;
    private Gender gender;
    private String cloudImageUrl;
    private LocalDateTime createdDate;
    private String createdBy;
    private boolean verified;
    private boolean enable;
    private List<String> roles;
}
