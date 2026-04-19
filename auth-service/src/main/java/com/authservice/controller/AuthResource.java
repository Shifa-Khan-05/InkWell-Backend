//package com.authservice.controller;
//
//import com.authservice.dto.UserRegistrationDTO;
//import com.authservice.dto.UserResponseDTO;
//import com.authservice.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthResource {
//
//	private final AuthService authService;
//
//	@PostMapping("/register")
//	public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegistrationDTO userDto) {
//		return ResponseEntity.ok(authService.register(userDto));
//	}
//
//	@PostMapping("/login")
//	public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
//	    try {
//	        String email = creds.get("email");
//	        String password = creds.get("password");
//	        
//	        // 1. Get Token
//	        String token = authService.login(email, password);
//	        
//	        // 2. Get Role
//	        String role = authService.getRoleByEmail(email);
//
//	        // 3. Wrap in Map to match Frontend expectation
//	        Map<String, String> response = new HashMap<>();
//	        response.put("token", token);
//	        response.put("role", role);
//	        
//	        return ResponseEntity.ok(response);
//	    } catch (Exception e) {
//	        // This sends the actual error message (e.g., "User not found") back to React
//	        return ResponseEntity.status(500).body(e.getMessage());
//	    }
//	}
//
//	@PostMapping("/logout")
//	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
//		authService.logout(token);
//		return ResponseEntity.noContent().build();
//	}
//
//	@GetMapping("/profile/{userId}")
//	public ResponseEntity<UserResponseDTO> getProfile(@PathVariable int userId) {
//		return ResponseEntity.ok(authService.getUserById(userId));
//	}
//
//	@PutMapping("/profile/{userId}")
//	public ResponseEntity<UserResponseDTO> updateProfile(@PathVariable int userId,
//			@RequestBody UserRegistrationDTO userDto) {
//
//		return ResponseEntity.ok(authService.updateProfile(userId, userDto));
//	}
//
//}


package com.authservice.controller;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegistrationDTO userDto) {
        return ResponseEntity.ok(authService.register(userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        try {
            String email = creds.get("email");
            String password = creds.get("password");
            
            // 1. Get Token from Service logic
            String token = authService.login(email, password);
            
            // 2. Get Role specifically for Frontend logic
            String role = authService.getRoleByEmail(email);

            // 3. Wrap in Map to match Dashboard.jsx expectations
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", role);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return 401 Unauthorized for bad credentials instead of 500
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server Error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponseDTO> getProfile(@PathVariable int userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserResponseDTO> updateProfile(@PathVariable int userId,
            @RequestBody UserRegistrationDTO userDto) {
        return ResponseEntity.ok(authService.updateProfile(userId, userDto));
    }
}