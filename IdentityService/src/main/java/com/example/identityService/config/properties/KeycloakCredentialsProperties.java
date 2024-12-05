package com.example.identityService.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "keycloak.credentials")
public class KeycloakCredentialsProperties {
    private String clientId;
    private String secret;
    private String scope;
    private String grantType;
    private String authServer;
}