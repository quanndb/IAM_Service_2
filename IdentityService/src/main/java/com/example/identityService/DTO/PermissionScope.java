package com.example.identityService.DTO;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PermissionScope {
    READ("READ"),
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String scope;

    @JsonValue
    public String getScope() {
        return scope;
    }

    @JsonCreator
    public static PermissionScope fromScope(String scope) {
        for (PermissionScope permissionScope : PermissionScope.values()) {
            if (permissionScope.scope.equalsIgnoreCase(scope)) {
                return permissionScope;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + scope);
    }
}
