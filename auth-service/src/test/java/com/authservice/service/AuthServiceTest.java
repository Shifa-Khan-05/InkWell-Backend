package com.authservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    void register_ShouldReturnUserWithRoleReader() {
        UserRegistrationDTO regDto = new UserRegistrationDTO("test", "test@ink.com", "pass", "Test User", null);
        User savedUser = new User();
        savedUser.setRole("ROLE_READER");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO response = authService.register(regDto);
        assertEquals("ROLE_READER", response.getRole());
    }
}