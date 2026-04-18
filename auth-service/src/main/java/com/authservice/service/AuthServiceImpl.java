package com.authservice.service;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AuthServiceImpl
 * Implements identity logic using DTOs to decouple the API from the Database.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public UserResponseDTO register(UserRegistrationDTO regDto) {
        // Checking for duplicate email
        if (userRepository.existsByEmail(regDto.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // Map DTO to Entity
        User user = new User();
        user.setUsername(regDto.getUsername());
        user.setEmail(regDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(regDto.getPassword()));
        user.setFullName(regDto.getFullName());
        user.setRole("READER"); // Default role [cite: 187]

        User savedUser = userRepository.save(user);

        // Map Entity back to Response DTO
        return new UserResponseDTO(
            savedUser.getUserId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getFullName()
        );
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials!");
        }

        return jwtUtils.generateToken(user.getEmail());
    }
}