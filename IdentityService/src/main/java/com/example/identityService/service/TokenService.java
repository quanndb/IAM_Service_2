package com.example.identityService.service;

import com.example.identityService.Util.TimeConverter;
import com.example.identityService.config.AuthenticationProperties;
import com.example.identityService.entity.Account;
import com.example.identityService.entity.Role;
import com.example.identityService.DTO.Token;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.repository.RoleRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthenticationProperties.class)
public class TokenService implements InitializingBean {

    @Value(value = "${security.authentication.jwt.access-token-life-time}")
    private String ACCESS_TOKEN_LIFE_TIME;
    @Value(value = "${security.authentication.jwt.refresh-token-life-time}")
    private String REFRESH_TOKEN_LIFE_TIME;
    @Value(value = "${security.authentication.jwt.email-token-life-time}")
    private String EMAIL_TOKEN_LIFE_TIME;

    private KeyPair keyPair;
    private final AuthenticationProperties properties;

    private final RedisTemplate<String, String> redisTemplate;

    private final RoleRepository roleRepository;

    @Override
    public void afterPropertiesSet() {
        this.keyPair = getKeyPair(properties.getKeyStore(),
                properties.getKeyStorePassword(),
                properties.getKeyAlias());
    }

    private KeyPair getKeyPair(String keyStore, String password, String alias){
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(keyStore), password.toCharArray());
        return keyStoreKeyFactory.getKeyPair(alias);
    }


    // token generators
    public String generateRefreshToken(String email, String ip){
        return otherTokenFactory(email, REFRESH_TOKEN_LIFE_TIME, ip);
    }

    public String generateTempEmailToken(String email, String ip){
        return otherTokenFactory(email, EMAIL_TOKEN_LIFE_TIME, ip);
    }

    public String accessTokenFactory(Account account) {
        // get the permissions
        Role role = roleRepository.findById(account.getRoleId())
                .orElseThrow(()->new AppExceptions(ErrorCode.ROLE_NOTFOUND));

        String tokenId = UUID.randomUUID().toString();

        // build token
        return Jwts.builder()
                .subject(account.getEmail())
                .issuer("DevDeli")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeConverter.convertToMilliseconds(ACCESS_TOKEN_LIFE_TIME)))
                .id(tokenId)
                .claim("scope", role.getName())
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    public String otherTokenFactory(String email, String liveTime, String ip) {
        String tokenId = UUID.randomUUID().toString();
        // build token
        return Jwts.builder()
                .subject(email)
                .issuer("DevDeli")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeConverter.convertToMilliseconds(liveTime)))
                .id(tokenId)
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .claim("IP", ip)
                .compact();
    }


    // utilities

    public Jwt getTokenDecoded(String token){
       return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build().decode(token);
    }


    public boolean verifyToken(String token){
        return !isTokenExpired(token) && !isLogout(token);
    }

    public Claims extractClaims(String token){
        try{
            return Jwts.parser().verifyWith(keyPair.getPublic())
                    .build().parseSignedClaims(token).getPayload();
        }
        catch (ExpiredJwtException exception){
            throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
        }
    }

    public boolean isTokenExpired(String token){
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean isLogout(String token){
        String tokenId = extractClaims(token).getId();
        String valueOfLogoutToken = redisTemplate.opsForValue().get("token_id:"+tokenId);
        return valueOfLogoutToken != null;
    }

    public boolean deActiveToken(Token token){
        Jwt decodedAccessToken = getTokenDecoded(token.getValue());
        try{
            redisTemplate.opsForValue()
                    .set("token_id:"+decodedAccessToken.getId(),
                            Objects.requireNonNull(decodedAccessToken.getExpiresAt()).toString(),
                            Duration.ofMillis(token.getLifeTime()));
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}
