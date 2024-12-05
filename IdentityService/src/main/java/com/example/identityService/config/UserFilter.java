package com.example.identityService.config;

import com.example.identityService.config.idp_config.IdpProvider;
import com.example.identityService.entity.Account;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.service.auth.AbstractAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserFilter extends OncePerRequestFilter {

    @Value("${app.idp}")
    private IdpProvider idpProvider;

    private final KeycloakProvider keycloakProvider;
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Jwt jwt = (Jwt) jwtAuthenticationToken.getCredentials();
            String email = jwt.getClaim("email");
            Account foundAccount = accountRepository.findByEmail(email)
                    .orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));

            if (!AbstractAuthService.isValidUserStatus(foundAccount)
                    || (idpProvider.equals(IdpProvider.KEYCLOAK) && !isTokenValid(jwt))) {
                throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenValid(Jwt token){
        List<UserSessionRepresentation> sessions = keycloakProvider
                .getRealmResourceWithAdminPrivilege().users().get(token.getSubject()).getUserSessions();

        return sessions.stream().anyMatch(session -> token.getClaim("sid").toString()
                .equals(session.getId()));
    }
}
