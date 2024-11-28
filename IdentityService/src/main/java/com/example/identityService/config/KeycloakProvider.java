package com.example.identityService.config;

import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KeycloakProvider {

    private final Keycloak keycloak = Keycloak.getInstance(
            "http://localhost:8081/auth",
            "IAM2",
            "admin",
            "1234",
            "iam-client",
            "vUAlBnG43sreZsAr7hvdqOz5S9FYz0Il"
    );
    private final RealmResource realmResource = keycloak.realm("IAM2");
}
