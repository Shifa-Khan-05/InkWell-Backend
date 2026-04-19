package com.authservice.service;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;

public interface AuthService {

    UserResponseDTO register(UserRegistrationDTO regDto);

    String login(String email, String password);

    void logout(String token);

    UserResponseDTO getUserById(int userId);

    UserResponseDTO updateProfile(int userId, UserRegistrationDTO userDto);

    void deactivateAccount(int userId);

    String getRoleByEmail(String email); // ✅ FIXED PARAM NAME

	String processOAuthPostLogin(String email, String name, String provider);
    
    
}