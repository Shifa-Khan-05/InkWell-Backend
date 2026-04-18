package com.authservice.service;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;

public interface AuthService {

	UserResponseDTO register(UserRegistrationDTO regDto);
	

	String login(String email, String password);
}