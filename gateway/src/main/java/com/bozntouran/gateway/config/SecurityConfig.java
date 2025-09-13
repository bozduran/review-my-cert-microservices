package com.bozntouran.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain gatewaySecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable CSRF if not needed
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/headerrouting/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/eureka/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/**","/css/**").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/openapi/**").permitAll()
                        .requestMatchers("/config/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers(GET,"/review","/review/**").permitAll()
                        .requestMatchers(GET, "/certificate","/certificate/**").permitAll()
                        .requestMatchers(GET, "/company","/company/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}