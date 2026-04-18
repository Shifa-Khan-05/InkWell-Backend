package com.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig handles the security configuration for the Auth-Service. It
 * manages password hashing and defines access rules for the identity
 * foundation. [cite: 185]
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		// Passwords must be stored as bcrypt hashes for security compliance
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()) // Disabling CSRF for our stateless APIs
				.authorizeHttpRequests(auth -> auth
						// 1. Permit Auth Endpoints
						.requestMatchers("/auth/register", "/auth/login").permitAll()

						// 2. Permit Swagger using the CUSTOM paths from your application.yml
						.requestMatchers("/api-docs/**").permitAll() // This matches your YAML setting
						.requestMatchers("/swagger-ui/**").permitAll().requestMatchers("/swagger-ui.html").permitAll()

						// 3. Secure everything else
						.anyRequest().authenticated());

		return http.build();
	}
}