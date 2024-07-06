package com.example.demokeycloak22.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorise ->
                authorise
                        .requestMatchers("/")
                        .permitAll()
                        .anyRequest()
                        .authenticated());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(
            customizer -> customizer.jwtAuthenticationConverter(jwt -> {
                Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
                Map<String, Object> claims = jwt.getClaims();
                String username = (String) claims.get("preferred_username");
                return new JwtAuthenticationToken(jwt, authorities, username);
            })
        ));

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(auth -> auth
//                .requestMatchers(new AntPathRequestMatcher("/"))
//                .permitAll()
//                .anyRequest()
//                .authenticated());
//
//        http.oauth2ResourceServer(oauth2 -> oauth2
//                .jwt(x -> {}));
//
//        return http.build();
//    }

//    @Bean
//    public JwtDecoder jwtDecoder() {
//        String jwkSetUri = "http://localhost:8080/realms/demo-keycloak22/protocol/openid-connect/certs";
//        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
//    }
}
