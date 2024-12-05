package com.example.identityService.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePageRequest extends PageRequest{
    private String id = "";
    private String name = "";
    private String description = "";
    private boolean deleted = false;
}
