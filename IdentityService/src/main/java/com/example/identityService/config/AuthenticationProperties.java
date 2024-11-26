package com.example.identityService.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "security.authentication.jwt")
public class AuthenticationProperties {
    private String keyStore;
    private String keyStorePassword;
    private String keyAlias;
}
