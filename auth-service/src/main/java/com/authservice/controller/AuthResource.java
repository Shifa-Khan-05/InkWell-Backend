package com.authservice.controller;

import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
// ❌ REMOVED @CrossOrigin to prevent duplicate header conflict with Gateway
public class AuthResource {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
		try {
			String email = creds.get("email");
			String password = creds.get("password");
			String token = authService.login(email, password);
			UserResponseDTO user = authService.findByEmail(email);

			Map<String, Object> response = new HashMap<>();
			response.put("token", token);
			response.put("role", user.getRole());
			response.put("userId", user.getUserId());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
		}
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegistrationDTO userDto) {
		return ResponseEntity.ok(authService.register(userDto));
	}

	@GetMapping("/profile/{userId}")
	public ResponseEntity<?> getProfile(@PathVariable int userId) {
		try {
			UserResponseDTO user = authService.getUserById(userId);
			return ResponseEntity.ok(user);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
	}

	@PutMapping("/profile/{userId}")
	public ResponseEntity<UserResponseDTO> updateProfile(@PathVariable int userId, @RequestBody ProfileUpdateDTO dto) {
		return ResponseEntity.ok(authService.updateProfile(userId, dto));
	}

	@PutMapping(value = "/profile/{userId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponseDTO> updateProfileWithImage(@PathVariable int userId,
			@RequestParam("fullName") String fullName, @RequestParam(value = "bio", required = false) String bio,
			@RequestParam(value = "image", required = false) MultipartFile image) {
		return ResponseEntity.ok(authService.updateProfileWithFile(userId, fullName, bio != null ? bio : "", image));
	}
}