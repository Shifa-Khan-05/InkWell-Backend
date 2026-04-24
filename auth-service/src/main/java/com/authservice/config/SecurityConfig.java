package com.authservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private OAuthSuccessHandler successHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// ✅ HIGH PRIORITY: Internal Upgrade PUT call
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/auth/users/*/upgrade").permitAll()

						// ✅ Public endpoints
						.requestMatchers("/auth/**").permitAll().requestMatchers("/oauth2/**", "/login/oauth2/**")
						.permitAll().requestMatchers("/uploads/**").permitAll()

						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					// Returns JSON error instead of redirecting to a login page
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized Access");
				})).oauth2Login(oauth2 -> oauth2.successHandler(successHandler));

		return http.build();
	}
}