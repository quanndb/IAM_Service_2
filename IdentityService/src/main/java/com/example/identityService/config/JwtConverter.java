package com.example.identityService.config;

import com.example.identityService.config.properties.JwtConverterProperties;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakProvider keycloakProvider;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final JwtConverterProperties properties;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if(!isTokenValid(jwt)) throw new AppExceptions(ErrorCode.UNAUTHENTICATED);

        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream()).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("email"));
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("permissions");
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        if (resourceAccess == null
                || (resource = (Map<String, Object>) resourceAccess.get(properties.getResourceId())) == null
                || (resourceRoles = (Collection<String>) resource.get("roles")) == null) {
            return Set.of();
        }
        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    private boolean isTokenValid(Jwt token){
        List<UserSessionRepresentation> sessions = keycloakProvider
                .getRealmResourceWithAdminPrivilege().users().get(token.getSubject()).getUserSessions();

        return sessions.stream().anyMatch(session -> token.getClaim("sid").toString()
                .equals(session.getId()));
    }
}