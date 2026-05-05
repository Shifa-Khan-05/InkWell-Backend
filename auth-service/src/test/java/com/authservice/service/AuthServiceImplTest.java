package com.authservice.service;

import com.authservice.client.NotificationClient;
import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.RoleRequest;
import com.authservice.entity.User;
import com.authservice.repository.RoleRequestRepository;
import com.authservice.repository.UserRepository;
import com.authservice.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRequestRepository roleRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1);
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setPasswordHash("hashedpassword");
        user.setRole("ROLE_READER");
        user.setActive(true);
        user.setFullName("Test User");
    }

    // --- EXISTING TESTS (KEEP THESE) ---

    @Mock
    private com.authservice.repository.EmailOtpRepository emailOtpRepository;

    @Test
    void register_Success() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("new@test.com");
        dto.setUsername("newuser");
        dto.setPassword("password");
        dto.setRole("AUTHOR");
        dto.setOtp("123456");

        com.authservice.entity.EmailOtp emailOtp = new com.authservice.entity.EmailOtp();
        emailOtp.setEmail("new@test.com");
        emailOtp.setOtp("123456");
        emailOtp.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(emailOtpRepository.findByEmail("new@test.com")).thenReturn(Optional.of(emailOtp));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = authService.register(dto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(emailOtpRepository).delete(emailOtp);
        verify(notificationClient).sendStyledEmail(anyMap());
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);
        when(jwtUtils.generateToken("test@test.com")).thenReturn("token");

        String token = authService.login("test@test.com", "password");

        assertThat(token).isEqualTo("token");
    }

    // --- NEW TESTS TO INCREASE COVERAGE TO >80% ---

    /**
     * Covers the branch where the 'uploads/' directory already exists.
     * This hits the 'if (!Files.exists(uploadPath))' false branch.
     */
    @Test
    void updateProfileWithFile_DirectoryAlreadyExists() throws Exception {
        Path path = Paths.get("uploads/");
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());

        UserResponseDTO result = authService.updateProfileWithFile(1, "Name", "user", "bio", 25, "pass", file);

        assertThat(result).isNotNull();
        assertThat(user.getProfileImageUrl()).contains("uploads/user_1");
    }

    /**
     * Covers the catch block in requestRoleChange when notification fails.
     */
    @Test
    void requestRoleChange_NotificationThrowsException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(roleRequestRepository.existsByUserUserIdAndStatus(1, "PENDING")).thenReturn(false);
        
        List<User> admins = new ArrayList<>();
        admins.add(user);
        when(userRepository.findByRole(anyString())).thenReturn(admins);
        
        // Force the notification client to throw an exception to hit the catch block
        doThrow(new RuntimeException("Service Down")).when(notificationClient).sendNotification(anyMap());

        // This should not throw an exception to the caller because of the try-catch in Service
        authService.requestRoleChange(1, "AUTHOR");

        verify(roleRequestRepository).save(any(RoleRequest.class));
        verify(notificationClient, atLeastOnce()).sendNotification(anyMap());
    }

    /**
     * Covers the branch where status is NOT "APPROVED" in processRoleRequest.
     */
    @Test
    void processRoleRequest_RejectedStatus() {
        RoleRequest req = new RoleRequest();
        req.setRequestId(1);
        req.setUser(user);
        req.setRequestedRole("AUTHOR");

        when(roleRequestRepository.findById(1)).thenReturn(Optional.of(req));

        authService.processRoleRequest(1, "REJECTED");

        assertThat(req.getStatus()).isEqualTo("REJECTED");
        // Verify updateUserRole logic (userRepository.save) is NEVER called
        verify(userRepository, never()).save(any(User.class));
        verify(roleRequestRepository).save(req);
    }

    /**
     * Covers the case where no admins are found to notify.
     */
    @Test
    void requestRoleChange_EmptyAdminList() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(roleRequestRepository.existsByUserUserIdAndStatus(1, "PENDING")).thenReturn(false);
        when(userRepository.findByRole(anyString())).thenReturn(new ArrayList<>());

        authService.requestRoleChange(1, "AUTHOR");

        verify(roleRequestRepository).save(any(RoleRequest.class));
        verify(notificationClient, never()).sendNotification(anyMap());
    }

    /**
     * Covers verification of all date fields in upgradeToPremium.
     */
    @Test
    void upgradeToPremium_SuccessWithFullData() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        authService.upgradeToPremium(1);

        assertThat(user.getRole()).isEqualTo("ROLE_PREMIUM");
        assertThat(user.getMembershipLevel()).isEqualTo("PREMIUM");
        assertThat(user.getSubscriptionStartDate()).isNotNull();
        assertThat(user.getSubscriptionEndDate()).isAfter(user.getSubscriptionStartDate());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfileWithFile_WithImageIOException() throws Exception {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        
        // Mock a file that throws IOException on getInputStream
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenThrow(new IOException("Simulated disk error"));

        assertThatThrownBy(() -> authService.updateProfileWithFile(1, null, null, null, null, null, file))
                .isInstanceOf(com.authservice.exception.BadRequestException.class)
                .hasMessageContaining("File storage failed");
    }
}