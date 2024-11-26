package com.example.identityService.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumRole {
    ADMIN("1"),
    USER("2");

    private final String id;
}
