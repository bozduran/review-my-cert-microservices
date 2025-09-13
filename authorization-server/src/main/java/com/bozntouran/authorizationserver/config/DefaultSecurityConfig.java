package com.bozntouran.authorizationserver.config;


import static org.springframework.security.config.Customizer.withDefaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Joe Grandja
 * @since 0.1.0
 */
@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSecurityConfig.class);

    // formatter:off
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(withDefaults());
        return http.build();
    }
    // formatter:on

    // @formatter:off
    @Bean
    public UserDetailsService users() {
        @SuppressWarnings("deprecation") // Ok for test purposes, not for production use
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("u")
                .password("p")
                .roles("USER")
                .build();

        UserDetails user1 = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        UserDetails user2 = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("USER", "ADMIN")
                .build();

        UserDetails user3 = User.withDefaultPasswordEncoder()
                .username("editor")
                .password("editor123")
                .roles("EDITOR")
                .build();

        return new InMemoryUserDetailsManager(user, user1, user2, user3);
    }
    // @formatter:on

}
//CHECKSTYLE:ON