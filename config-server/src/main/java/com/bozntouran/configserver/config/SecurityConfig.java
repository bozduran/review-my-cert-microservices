package com.bozntouran.configserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Disable CSRF to allow POST to /encrypt and /decrypt endpoints
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests ->
                        requests.requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService users() {

        UserDetails user = User.withDefaultPasswordEncoder()
                .username("dev-usr")
                .password("dev-pwd")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

}
