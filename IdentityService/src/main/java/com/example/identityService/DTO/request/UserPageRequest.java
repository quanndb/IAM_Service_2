package com.example.identityService.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPageRequest extends PageRequest{
    private String id = "";
    private String email = "";
    private String fullname = "";
    private boolean verified = true;
    private boolean enable = true;
    private boolean deleted = false;
    private String gender = "";
    private String address = "";
    private String cloudImageId = "";
    private String cloudImageUrl = "";
}
