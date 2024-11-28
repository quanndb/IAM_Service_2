package com.example.identityService.repository.keyCloakRepositories;

import com.example.identityService.DTO.request.GetKeycloakTokenRequest;
import com.example.identityService.DTO.request.KeycloakLoginRequest;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "authClient", url = "http://localhost:8081/auth/realms/IAM2/protocol/openid-connect")
public interface AuthClient {

    @GetMapping(value = "/userinfo")
    Object getUserInfo(@RequestHeader("Authorization") String authorizationHeader);

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Object loginWithKeycloak(@RequestBody Map<String, Object> keycloakLoginRequest);

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Object getNewToken(@RequestBody Map<String, Object>  keycloakTokenRequest);
}
