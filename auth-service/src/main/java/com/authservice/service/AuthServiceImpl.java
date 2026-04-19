package com.authservice.service;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        user.setPasswordHash(passwordEncoder.encode(regDto.getPassword()));
        user.setFullName(regDto.getFullName());
        user.setRole("ROLE_READER"); // Requirement 2.3: Default role
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is suspended!");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials!");
        }

        return jwtUtils.generateToken(user.getEmail());
    }

//    // ✅ Logic for Google/GitHub Post-Login Processing
//    @Override
//    public String processOAuthPostLogin(String email, String name, String provider) {
//        User user = userRepository.findByEmail(email).orElseGet(() -> {
//            User newUser = new User();
//            newUser.setEmail(email);
//            newUser.setFullName(name);
//            newUser.setUsername(email.split("@")[0] + "_" + provider.toLowerCase());
//            newUser.setRole("ROLE_READER");
//            newUser.setActive(true);
//            // OAuth users don't have a local password
//            newUser.setPasswordHash("OAUTH_USER_EXTERNAL"); 
//            return userRepository.save(newUser);
//        });
//        return jwtUtils.generateToken(user.getEmail());
//    }

    @Override
    public UserResponseDTO getUserById(int userId) {
        User user = userRepository.findById(userId)
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
    public void logout(String token) {
        // Logic for token blacklisting can be added here
        System.out.println("User logged out with token: " + token);
    }

    @Override
    public UserResponseDTO updateProfile(int userId, UserRegistrationDTO updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        if (updateDto.getFullName() != null) user.setFullName(updateDto.getFullName());
        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }

    @Override
    public void deactivateAccount(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setActive(false);
        userRepository.save(user);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFullName()
        );
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
            newUser.setPasswordHash("OAUTH_USER_EXTERNAL"); 
            return userRepository.save(newUser);
        });
        return jwtUtils.generateToken(user.getEmail());
    }
}