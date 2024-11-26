package com.example.identityService.DTO.request;

import com.example.identityService.DTO.Gender;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {
    private String fullname;
    private String address;
    private Gender gender;
    private String cloudImageUrl;
}
