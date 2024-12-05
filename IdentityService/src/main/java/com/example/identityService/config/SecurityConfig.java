package com.example.identityService.config;

import com.example.identityService.config.idp_config.IdpProvider;
import com.example.identityService.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    String[] PUBLIC_ENDPOINTS = {
            "auth/**",
            "swagger-ui/**",
            "api-docs/**",
            "v3/api-docs/**",
    };

    @Value("${app.idp}")
    private IdpProvider idpProvider;

    private final JwtConverter jwtConverter;
    private final TokenService tokenService;
    private final UserFilter userFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());

        switch (idpProvider){
            case IdpProvider.KEYCLOAK -> configureKeycloak(http);
            default -> configureCustomJwt(http);
        }

        http.addFilterAfter(userFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    private void configureKeycloak(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
    }

    private void configureCustomJwt(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(new CustomJwtDecoder(tokenService)))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator customPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}
