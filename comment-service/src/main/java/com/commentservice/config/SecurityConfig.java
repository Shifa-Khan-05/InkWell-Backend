package com.commentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// DO THIS IN ALL SERVICES (Post, Auth, Comment)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .cors(cors -> cors.disable()) // ✅ Explicitly disable local CORS
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
	    return http.build();
	}
}