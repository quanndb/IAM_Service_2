package com.example.identityService.config;

import com.example.identityService.exception.JwtAuthenticationException;
import com.example.identityService.service.TokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    private final TokenService tokenService;

    @Override
    public Jwt decode(String token){
        boolean isValidToken = tokenService.verifyToken(token);
        Claims claims = tokenService.extractClaims(token);
        var scopeClaim = claims == null ? null : claims.get("scope");

        if (!isValidToken || scopeClaim == null) {
            try {
                throw new JwtAuthenticationException("Invalid token or missing scope claim");
            } catch (JwtAuthenticationException _) {
            }
        }

        return tokenService.getTokenDecoded(token);
    }
}
