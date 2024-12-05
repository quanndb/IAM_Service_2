package com.example.identityService.DTO.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionResponse {
    private String id;
    private String name;
    private String code;
    private boolean deleted;
}
