package com.example.identityService.DTO.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionPageRequest extends PageRequest{
    private String id = "";
    private String name = "";
    private String code = "";
    private boolean deleted = false;
}
