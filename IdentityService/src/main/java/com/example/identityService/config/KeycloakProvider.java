package com.example.identityService.config;

import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KeycloakProvider {

    @Value("${keycloak.auth-server-url}")
    private String KEYCLOAK_AUTH_URL;
    @Value("${keycloak.realm}")
    private String REALM;
    @Value("${keycloak.credentials.client-id}")
    private String CLIENT_ID;
    @Value("${keycloak.credentials.secret}")
    private String CLIENT_SECRET;
    @Value("${keycloak.credentials.username}")
    private String ADMIN;
    @Value("${keycloak.credentials.password}")
    private String PASSWORD;

    private Keycloak keycloak;
    private RealmResource realmResource;

    public RealmResource getRealmResourceWithAdminPrivilege(){
        setKeycloak(ADMIN,PASSWORD);
        return realmResource;
    }

    public Keycloak getKeycloakWithAdminPrivilege(){
        setKeycloak(ADMIN, PASSWORD);
        return keycloak;
    }

    public void setKeycloak(String username, String password) {
        this.keycloak = Keycloak.getInstance(
                KEYCLOAK_AUTH_URL,
                REALM,
                username,
                password,
                CLIENT_ID,
                CLIENT_SECRET
        );
        this.realmResource = keycloak.realm(REALM);
    }
}
