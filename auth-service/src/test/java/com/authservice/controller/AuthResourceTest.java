package com.authservice.controller;

import com.authservice.client.NotificationClient;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AuthResource authResource;

    private UserResponseDTO userResponseDTO;
    private User user;

    @BeforeEach
    void setUp() {
        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUserId(1);
        userResponseDTO.setEmail("test@test.com");
        userResponseDTO.setRole("USER");

        user = new User();
        user.setUserId(1);
        user.setEmail("test@test.com");
    }

    @Test
    void forgotPassword_Success() {
        Map<String, String> request = Map.of("email", "test@test.com");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        doNothing().when(notificationClient).sendStyledEmail(anyMap());

        ResponseEntity<?> response = authResource.forgotPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(userRepository).save(any(User.class));
        verify(notificationClient).sendStyledEmail(anyMap());
    }

    @Test
    void forgotPassword_NotFound() {
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                com.authservice.exception.ResourceNotFoundException.class,
                () -> authResource.forgotPassword(Map.of("email", "test@test.com"))
        );
    }

    @Test
    void resetPassword_Success() {
        user.setTokenExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByResetToken("valid-token"))
                .thenReturn(Optional.of(user));

        ResponseEntity<?> response =
                authResource.resetPassword(Map.of("token", "valid-token", "newPassword", "newPass"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).resetUserPassword(user, "newPass");
    }

    @Test
    void resetPassword_Expired() {
        user.setTokenExpiry(LocalDateTime.now().minusHours(1));

        when(userRepository.findByResetToken("token"))
                .thenReturn(Optional.of(user));

        assertThrows(
                com.authservice.exception.AuthException.class,
                () -> authResource.resetPassword(Map.of("token", "token", "newPassword", "pass"))
        );
    }

    @Test
    void login_Success() {
        when(authService.login("test@test.com", "password"))
                .thenReturn("dummy-token");

        when(authService.findByEmail("test@test.com"))
                .thenReturn(userResponseDTO);

        ResponseEntity<?> response =
                authResource.login(Map.of("email", "test@test.com", "password", "password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void login_Failure() {
        when(authService.login("test@test.com", "wrong"))
                .thenThrow(new RuntimeException());

        ResponseEntity<?> response =
                authResource.login(Map.of("email", "test@test.com", "password", "wrong"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void upgradeUser_Success() {
        doNothing().when(authService).upgradeToPremium(1);

        ResponseEntity<?> response = authResource.upgradeUser(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void upgradeUser_Failure() {
        doThrow(new RuntimeException()).when(authService).upgradeToPremium(1);

        ResponseEntity<?> response = authResource.upgradeUser(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        ResponseEntity<List<User>> response = authResource.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(authService).deleteUser(1);

        ResponseEntity<Void> response = authResource.deleteUser(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}