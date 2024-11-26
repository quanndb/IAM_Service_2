package com.example.identityService.config;

import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    private final TokenService tokenService;

    @Override
    public Jwt decode(String token){
        boolean isValidToken = tokenService.verifyToken(token);
        var scopeClaim = tokenService.extractClaims(token).get("scope");
        if(!isValidToken || scopeClaim == null) throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
        return tokenService.getTokenDecoded(token);
    }
}
