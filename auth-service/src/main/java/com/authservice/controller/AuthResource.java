package com.authservice.controller;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthResource Exposes /auth/register and /auth/login endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegistrationDTO userDto) {

		UserResponseDTO response = authService.register(userDto);

		return ResponseEntity.ok(response);
	}

	// (Optional improvement later: use LoginRequest DTO instead of Map)
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {

		String token = authService.login(credentials.get("email"), credentials.get("password"));

		return ResponseEntity.ok(token);
	}
}