package com.authservice.controller;

import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // ✅ CRITICAL for local React dev
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

	@GetMapping("/profile/{userId}")
	public ResponseEntity<UserResponseDTO> getProfile(@PathVariable int userId) {
		return ResponseEntity.ok(authService.getUserById(userId));
	}

	@PutMapping("/profile/{userId}")
	public ResponseEntity<UserResponseDTO> updateProfile(@PathVariable int userId, @RequestBody ProfileUpdateDTO dto) {
		return ResponseEntity.ok(authService.updateProfile(userId, dto));
	}

	// ✅ FIXED: Handling Multipart with Optional Bio to prevent 400 Bad Request
	@PutMapping(value = "/profile/{userId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponseDTO> updateProfileWithImage(@PathVariable int userId,
			@RequestParam("fullName") String fullName, @RequestParam(value = "bio", required = false) String bio,
			@RequestParam(value = "image", required = false) MultipartFile image) {

		return ResponseEntity.ok(authService.updateProfileWithFile(userId, fullName, bio != null ? bio : "", image));
	}
}