package com.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.util.JwtUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtils jwtUtils;

	@InjectMocks
	private AuthServiceImpl authService;

	@Test
	void register_ShouldReturnUserResponseDTO_WhenSuccessful() {
		// 1. Arrange
		UserRegistrationDTO regDto = new UserRegistrationDTO("shifa_k", "shifa@tit.com", "Student@123", "Shifa Khan");

		User savedUser = new User();
		savedUser.setUserId(1);
		savedUser.setUsername("shifa_k");
		savedUser.setEmail("shifa@tit.com");
		savedUser.setRole("READER");

		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		// 2. Act
		UserResponseDTO response = authService.register(regDto);

		// 3. Assert
		assertNotNull(response);
		assertEquals("READER", response.getRole());
		assertEquals("shifa_k", response.getUsername());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void login_ShouldReturnToken_WhenCredentialsAreValid() {
		// 1. Arrange
		User user = new User();
		user.setEmail("shifa@tit.com");
		user.setPasswordHash("hashed_pass");

		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		when(jwtUtils.generateToken(anyString())).thenReturn("mocked-jwt-token");

		// 2. Act
		String token = authService.login("shifa@tit.com", "Student@123");

		// 3. Assert
		assertEquals("mocked-jwt-token", token);
		verify(jwtUtils, times(1)).generateToken("shifa@tit.com");
	}
}