package com.example.identityService.repository.keyCloakRepositories;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile-client", url = "${app.keycloak.base-url}")
public interface ProfileClient {

    @GetMapping(value = "/userinfo")
    Object getUserInfo(@RequestHeader("Authorization") String authorizationHeader);
}
