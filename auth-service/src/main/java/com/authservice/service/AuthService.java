package com.authservice.service;

import org.springframework.web.multipart.MultipartFile;

import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;

public interface AuthService {

	UserResponseDTO register(UserRegistrationDTO regDto);

	String login(String email, String password);


	UserResponseDTO getUserById(int userId);

	UserResponseDTO findByEmail(String email);


	UserResponseDTO updateProfileWithFile(int userId, String fullName, String bio, MultipartFile image);

	String processOAuthPostLogin(String email, String name, String provider);

	void deactivateAccount(int userId);

	UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto);

	String getRoleByEmail(String email);

}