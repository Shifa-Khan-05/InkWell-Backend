package com.authservice.controller;

import com.authservice.client.NotificationClient;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

	private final AuthService authService;
	private final UserRepository userRepository;
	@Autowired
	private NotificationClient notificationClient; // Inject the client

	@PutMapping("/users/{id}/upgrade")
	public ResponseEntity<?> upgradeUser(@PathVariable Integer id) {
		try {
			authService.upgradeToPremium(id);
			return ResponseEntity.ok(Map.of("status", "success", "message", "User upgraded to PREMIUM successfully"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upgrade failed: " + e.getMessage());
		}
	}

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

	@PutMapping(value = "/profile/{userId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponseDTO> updateProfileWithImage(@PathVariable int userId,
			@RequestParam(value = "fullName", required = false) String fullName,
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "bio", required = false) String bio,
			@RequestParam(value = "age", required = false) Integer age,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "image", required = false) MultipartFile image) {
		return ResponseEntity
				.ok(authService.updateProfileWithFile(userId, fullName, username, bio, age, password, image));
	}

	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userRepository.findAll());
	}

	@PutMapping("/users/{userId}/role")
	public ResponseEntity<Void> updateRole(@PathVariable Integer userId, @RequestParam String newRole) {
		authService.updateUserRole(userId, newRole);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/users/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
		authService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/users/{userId}/status")
	public ResponseEntity<Void> toggleUserStatus(@PathVariable Integer userId, @RequestParam boolean active) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		user.setActive(active);
		userRepository.save(user);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/users/ids")
	public List<Integer> getAllUserIds() {
		return userRepository.findAll().stream().map(User::getUserId).toList();
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
		String token = request.get("token");
		String newPassword = request.get("newPassword");

		User user = userRepository.findByResetToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid or expired security token."));

		if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("Security token has expired.");
		}

		// Pass to service layer for encoded saving
		authService.resetUserPassword(user, newPassword);

		return ResponseEntity.ok(Map.of("message", "Security protocols updated successfully."));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Identity not found."));

		String token = UUID.randomUUID().toString();
		user.setResetToken(token);
		user.setTokenExpiry(LocalDateTime.now().plusHours(1));
		userRepository.save(user);

		String resetLink = "http://localhost:5173/reset-password?token=" + token;

		Map<String, String> mailData = new HashMap<>();
		mailData.put("recipientEmail", email);
		mailData.put("subject", "InkWell Security Reset");
		mailData.put("title", "Credential Recovery");
		mailData.put("body", "A request to reset your credentials was initiated. Click below to continue.");
		mailData.put("actionUrl", resetLink);

		notificationClient.sendStyledEmail(mailData);

		return ResponseEntity.ok(Map.of("message", "Recovery manuscript dispatched."));
	}

}