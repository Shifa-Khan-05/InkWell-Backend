package com.authservice.service;

import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public UserResponseDTO register(UserRegistrationDTO regDto) {
        log.info("Attempting to register new user with email: {}", regDto.getEmail());
        if (userRepository.existsByEmail(regDto.getEmail())) {
            log.warn("Registration failed: Email {} already exists", regDto.getEmail());
            throw new RuntimeException("Email already registered!");
        }
        User user = new User();
        user.setUsername(regDto.getUsername());
        user.setEmail(regDto.getEmail());
        // Use PasswordHash to match your entity field
        user.setPasswordHash(passwordEncoder.encode(regDto.getPassword()));
        user.setFullName(regDto.getFullName());
        
        String selectedRole = regDto.getRole() != null ? regDto.getRole() : "READER";
        user.setRole("ROLE_" + selectedRole.toUpperCase());
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with ID: {}", savedUser.getEmail(), savedUser.getUserId());
        return mapToResponseDTO(savedUser);
    }

    @Override
    public String login(String email, String password) {
        log.info("Authentication attempt for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: User with email {} not found", email);
                    return new RuntimeException("User not found!");
                });
        
        if (!user.isActive()) {
            log.warn("Login failed: Account {} is suspended", email);
            throw new RuntimeException("Account is suspended!");
        }
            
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed: Invalid credentials for email {}", email);
            throw new RuntimeException("Invalid credentials!");
        }
            
        log.info("User {} authenticated successfully", email);
        return jwtUtils.generateToken(user.getEmail());
    }

    @Override
    public UserResponseDTO getUserById(int userId) {
        log.debug("Fetching user profile by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        log.debug("Fetching user profile by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return mapToResponseDTO(user);
    }

    @Override
    public String getRoleByEmail(String email) {
        log.debug("Checking role for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return user.getRole();
    }

    @Override
    public UserResponseDTO updateProfileWithFile(int userId, String fullName, String username, String bio, Integer age, String password, MultipartFile image) {
        log.info("Identity update initiated for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
                
        if (fullName != null && !fullName.isEmpty())
            user.setFullName(fullName);
        if (username != null && !username.isEmpty())
            user.setUsername(username);
        if (bio != null)
            user.setBio(bio);
        if (age != null)
            user.setAge(age);
        if (password != null && !password.isEmpty()) {
            log.debug("Updating password for user ID: {}", userId);
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath))
                    Files.createDirectories(uploadPath);
                
                    
                String fileName = "user_" + userId + "_" + System.currentTimeMillis() + ".jpg";
                Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                
                // Assuming your resource handler maps /uploads/**
                user.setProfileImageUrl("http://localhost:8081/uploads/" + fileName);
                log.debug("Profile image updated for user ID: {}", userId);
            } catch (IOException e) {
                log.error("Image upload failed for user ID {}: {}", userId, e.getMessage());
                throw new RuntimeException("File storage failed: " + e.getMessage());
            }
        }
        User updatedUser = userRepository.save(user);
        log.info("Identity updated successfully for user ID: {}", userId);
        return mapToResponseDTO(updatedUser);
    }

    @Override
    public String processOAuthPostLogin(String email, String name, String provider) {
        log.info("Processing OAuth login via {}: {}", provider, email);
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating new OAuth user for email: {}", email);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setUsername(email.split("@")[0] + "_" + provider.toLowerCase());
            newUser.setRole("ROLE_READER");
            newUser.setActive(true);
            newUser.setPasswordHash("OAUTH_USER");
            return userRepository.save(newUser);
        });
        return jwtUtils.generateToken(user.getEmail());
    }

    @Override
    public void deactivateAccount(int userId) {
        log.info("Deactivating account ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto) {
        log.info("Basic profile update for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (updateDto.getFullName() != null)
            user.setFullName(updateDto.getFullName());
        if (updateDto.getBio() != null)
            user.setBio(updateDto.getBio());
        if (updateDto.getAge() != null) user.setAge(updateDto.getAge());
        
            
        return mapToResponseDTO(userRepository.save(user));
    }

    @Override
    public void updateUserRole(Integer userId, String newRole) {
        log.info("Updating role for user ID {} to {}", userId, newRole);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Identity not found"));

        String formattedRole = newRole.toUpperCase();
        if (!formattedRole.startsWith("ROLE_")) {
            formattedRole = "ROLE_" + formattedRole;
        }

        user.setRole(formattedRole);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        log.warn("Permanent deletion requested for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Identity does not exist");
        }
        userRepository.deleteById(userId);
    }
    
    @Override
    public Integer getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElse(null);
    }
    
    @Override
    public void upgradeToPremium(Integer userId) {
        log.info("Upgrading user ID {} to PREMIUM", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setRole("ROLE_PREMIUM");
        user.setMembershipLevel("PREMIUM");
        user.setSubscriptionStartDate(java.time.LocalDateTime.now());
        user.setSubscriptionEndDate(java.time.LocalDateTime.now().plusMonths(1));
        
        userRepository.save(user);
        log.info("User {} has been upgraded to PREMIUM successfully. Expiry: {}", userId, user.getSubscriptionEndDate());
    }

    // ✅ ADDED: Reset Password Implementation for Forgot Password flow
    @Override
    public void resetUserPassword(User user, String newPassword) {
        log.info("Resetting password for user ID: {}", user.getUserId());
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
            user.getUserId(), 
            user.getUsername(), 
            user.getEmail(), 
            user.getRole(),
            user.getFullName(), 
            user.getProfileImageUrl(),
            user.getBio(),
            user.getAge(),
            user.getSubscriptionStartDate(),
            user.getSubscriptionEndDate()
        );
    }
    
    
}