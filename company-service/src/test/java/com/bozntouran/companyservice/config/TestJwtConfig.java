package com.bozntouran.companyservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestJwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        // επιστρέφει mock για να μην προσπαθεί να καλέσει κανονικό Keycloak/Issuer
        return mock(JwtDecoder.class);
    }
}