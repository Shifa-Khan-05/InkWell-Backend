package com.authservice.service;

import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public UserResponseDTO register(UserRegistrationDTO regDto) {
        if (userRepository.existsByEmail(regDto.getEmail())) {
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
        
        return mapToResponseDTO(userRepository.save(user));
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        
        if (!user.isActive())
            throw new RuntimeException("Account is suspended!");
            
        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials!");
            
        return jwtUtils.generateToken(user.getEmail());
    }

    @Override
    public UserResponseDTO getUserById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return mapToResponseDTO(user);
    }

    @Override
    public String getRoleByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return user.getRole();
    }

    @Override
    public UserResponseDTO updateProfileWithFile(int userId, String fullName, String username, String bio, Integer age, String password, MultipartFile image) {
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
        if (password != null && !password.isEmpty())
            user.setPasswordHash(passwordEncoder.encode(password));

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
            } catch (IOException e) {
                throw new RuntimeException("File storage failed: " + e.getMessage());
            }
        }
        return mapToResponseDTO(userRepository.save(user));
    }

    @Override
    public String processOAuthPostLogin(String email, String name, String provider) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (updateDto.getFullName() != null)
            user.setFullName(updateDto.getFullName());
        if (updateDto.getBio() != null)
            user.setBio(updateDto.getBio());
        if (updateDto.getAge() != null) user.setAge(updateDto.getAge()); // ✅ Add this line
        
            
        return mapToResponseDTO(userRepository.save(user));
    }

    @Override
    public void updateUserRole(Integer userId, String newRole) {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setRole("ROLE_PREMIUM");
        userRepository.save(user);
        System.out.println("User " + userId + " has been upgraded to PREMIUM successfully.");
    }

    // ✅ ADDED: Reset Password Implementation for Forgot Password flow
    @Override
    public void resetUserPassword(User user, String newPassword) {
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
            user.getProfileImageUrl()
        );
    }
    
    
}