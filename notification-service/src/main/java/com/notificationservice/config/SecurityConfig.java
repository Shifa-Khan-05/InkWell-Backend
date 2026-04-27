package com.notificationservice.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            // Enable CORS properly
            .cors(cors -> {})

            .authorizeHttpRequests(auth -> auth

                // Allow Actuator for Spring Boot Admin
                .requestMatchers("/actuator/**").permitAll()

                // Allow Swagger if used
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Allow notification APIs
                .requestMatchers("/notifications/**").permitAll()

                // Everything else allowed
                .anyRequest().permitAll()
            );

        return http.build();
    }
}