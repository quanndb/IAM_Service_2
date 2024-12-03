package com.example.identityService.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumRole {
    USER("USER"),
    USER_ADMINISTRATOR("USER_ADMINISTRATOR"),
    SYSTEM_ADMINISTRATOR("SYSTEM_ADMINISTRATOR"),
    SUPPER_ADMIN("SUPPER_ADMIN");

    private final String name;

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static EnumRole fromName(String scope) {
        for (EnumRole enumRole : EnumRole.values()) {
            if (enumRole.name.equalsIgnoreCase(scope)) {
                return enumRole;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + scope);
    }
}
