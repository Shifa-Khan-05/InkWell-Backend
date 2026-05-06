package com.authservice.service;

import com.authservice.dto.ProfileUpdateDTO;

import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.entity.EmailOtp;
import com.authservice.repository.EmailOtpRepository;
import com.authservice.repository.UserRepository;
import com.authservice.repository.RoleRequestRepository;
import com.authservice.entity.RoleRequest;
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

    @org.springframework.beans.factory.annotation.Value("${gateway.url:https://3.108.190.193.nip.io}")
    private String gatewayUrl;

    private final UserRepository userRepository;
    private final RoleRequestRepository roleRequestRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final com.authservice.client.NotificationClient notificationClient;

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String USER_NOT_FOUND = "User not found!";
    private static final String IDENTITY_NOT_FOUND = "Identity not found";

    @Override
    public void sendRegistrationOtp(String email) {
        log.info("Sending registration OTP to {}", email);
        if (userRepository.existsByEmail(email)) {
            throw new com.authservice.exception.BadRequestException("Email already registered!");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        
        EmailOtp emailOtp = emailOtpRepository.findByEmail(email).orElse(new EmailOtp());
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setCreatedAt(java.time.LocalDateTime.now());
        emailOtp.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));
        emailOtpRepository.save(emailOtp);

        java.util.Map<String, String> mailData = new java.util.HashMap<>();
        mailData.put("recipientEmail", email);
        mailData.put("subject", "Email Verification OTP");
        mailData.put("title", "Verify Your Email");
        mailData.put("body", "Your OTP for InkWell registration is: " + otp + ". It expires in 10 minutes.");
        mailData.put("actionUrl", "http://localhost:5173/register"); // Default action link
        notificationClient.sendStyledEmail(mailData);
    }

    @Override
    public UserResponseDTO register(UserRegistrationDTO regDto) {
        log.info("Attempting to register new user with email: {}", regDto.getEmail());
        if (userRepository.existsByEmail(regDto.getEmail())) {
            log.warn("Registration failed: Email {} already exists", regDto.getEmail());
            throw new com.authservice.exception.BadRequestException("Email already registered!");
        }

        if (regDto.getOtp() == null || regDto.getOtp().isEmpty()) {
            throw new com.authservice.exception.BadRequestException("OTP is required for registration!");
        }

        EmailOtp emailOtp = emailOtpRepository.findByEmail(regDto.getEmail())
                .orElseThrow(() -> new com.authservice.exception.BadRequestException("No OTP found for this email. Please request a new one."));

        if (!emailOtp.getOtp().equals(regDto.getOtp())) {
            throw new com.authservice.exception.BadRequestException("Invalid OTP!");
        }

        if (emailOtp.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new com.authservice.exception.BadRequestException("OTP has expired. Please request a new one.");
        }

        // OTP Valid - Proceed with Registration
        User user = new User();
        user.setUsername(regDto.getUsername());
        user.setEmail(regDto.getEmail());
        // Use PasswordHash to match your entity field
        user.setPasswordHash(passwordEncoder.encode(regDto.getPassword()));
        user.setFullName(regDto.getFullName());
        
        String selectedRole = regDto.getRole() != null ? regDto.getRole() : "READER";
        user.setRole(ROLE_PREFIX + selectedRole.toUpperCase());
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        
        // Clean up OTP
        emailOtpRepository.delete(emailOtp);

        // Send Welcome Email
        java.util.Map<String, String> welcomeMailData = new java.util.HashMap<>();
        welcomeMailData.put("recipientEmail", savedUser.getEmail());
        welcomeMailData.put("subject", "Welcome to InkWell!");
        welcomeMailData.put("title", "Welcome Aboard!");
        welcomeMailData.put("body", "Thank you for registering on InkWell. We're excited to have you in our community.");
        welcomeMailData.put("actionUrl", "http://localhost:5173/login");
        notificationClient.sendStyledEmail(welcomeMailData);

        log.info("User registered successfully: {} with ID: {}", savedUser.getEmail(), savedUser.getUserId());
        return mapToResponseDTO(savedUser);
    }

    @Override
    public String login(String email, String password) {
        log.info("Authentication attempt for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: User with email {} not found", email);
                    return new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND);
                });
        
        if (!user.isActive()) {
            log.warn("Login failed: Account {} is suspended", email);
            throw new com.authservice.exception.AuthException("Account is suspended!");
        }
            
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed: Invalid credentials for email {}", email);
            throw new com.authservice.exception.AuthException("Invalid credentials!");
        }
            
        log.info("User {} authenticated successfully", email);
        return jwtUtils.generateToken(user.getEmail());
    }

    @Override
    public UserResponseDTO getUserById(int userId) {
        log.debug("Fetching user profile by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        log.debug("Fetching user profile by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        return mapToResponseDTO(user);
    }

    @Override
    public String getRoleByEmail(String email) {
        log.debug("Checking role for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        return user.getRole();
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.amazonaws.services.s3.AmazonS3 s3Client;

    @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket:inkwell-bucket}")
    private String bucketName;

    @Override
    public UserResponseDTO updateProfileWithFile(int userId, String fullName, String username, String bio, Integer age, String password, MultipartFile image) {
        log.info("Identity update initiated for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
                
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
                String fileName = "user_" + userId + "_" + System.currentTimeMillis() + ".jpg";
                
                if (s3Client != null) {
                    // Upload to S3
                    com.amazonaws.services.s3.model.ObjectMetadata metadata = new com.amazonaws.services.s3.model.ObjectMetadata();
                    metadata.setContentLength(image.getSize());
                    metadata.setContentType(image.getContentType());
                    s3Client.putObject(bucketName, "uploads/" + fileName, image.getInputStream(), metadata);
                    user.setProfileImageUrl(s3Client.getUrl(bucketName, "uploads/" + fileName).toString());
                } else {
                    // Fallback to local storage
                    String uploadDir = "uploads/";
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                    Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    user.setProfileImageUrl(gatewayUrl + "/uploads/" + fileName);
                }
                log.debug("Profile image updated for user ID: {}", userId);
            } catch (IOException e) {
                log.error("Image upload failed for user ID {}: {}", userId, e.getMessage());
                throw new com.authservice.exception.BadRequestException("File storage failed: " + e.getMessage());
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
            newUser.setRole(ROLE_PREFIX + "READER");
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
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto) {
        log.info("Basic profile update for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException("User not found"));
                
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
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(IDENTITY_NOT_FOUND));

        String formattedRole = newRole.toUpperCase();
        if (!formattedRole.startsWith(ROLE_PREFIX)) {
            formattedRole = ROLE_PREFIX + formattedRole;
        }

        user.setRole(formattedRole);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        log.warn("Permanent deletion requested for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new com.authservice.exception.ResourceNotFoundException("Identity does not exist");
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
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setRole(ROLE_PREFIX + "PREMIUM");
        user.setMembershipLevel("PREMIUM");
        user.setSubscriptionStartDate(java.time.LocalDateTime.now());
        user.setSubscriptionEndDate(java.time.LocalDateTime.now().plusMonths(1));
        
        userRepository.save(user);
        log.info("User {} has been upgraded to PREMIUM successfully. Expiry: {}", userId, user.getSubscriptionEndDate());
    }

    @Override
    public void resetUserPassword(User user, String newPassword) {
        log.info("Resetting password for user ID: {}", user.getUserId());
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public void requestRoleChange(int userId, String requestedRole) {
        log.info("Requesting role change for user ID {} to {}", userId, requestedRole);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException("User not found with ID: " + userId));
        
        if (roleRequestRepository.existsByUserUserIdAndStatus(userId, "PENDING")) {
            throw new com.authservice.exception.BadRequestException("A role change request is already pending.");
        }

        RoleRequest request = RoleRequest.builder()
                .user(user)
                .requestedRole(requestedRole)
                .status("PENDING")
                .requestedAt(java.time.LocalDateTime.now())
                .build();
        roleRequestRepository.save(request);

        // Notify admins
        try {
            java.util.List<User> admins = new java.util.ArrayList<>(userRepository.findByRole(ROLE_PREFIX + "ADMIN"));
            admins.addAll(userRepository.findByRole("ADMIN"));
            for (User admin : admins) {
                java.util.Map<String, Object> notification = new java.util.HashMap<>();
                notification.put("recipientId", admin.getUserId());
                notification.put("actorId", user.getUserId());
                notification.put("type", "SYSTEM");
                notification.put("message", user.getFullName() + " has requested the " + requestedRole + " role.");
                notification.put("relatedId", user.getUserId());
                notificationClient.sendNotification(notification);
            }
        } catch (Exception e) {
            log.error("Failed to send admin notifications for role request: ", e);
        }
    }

    @Override
    public java.util.List<RoleRequest> getAllRoleRequests() {
        return roleRequestRepository.findAll();
    }

    @Override
    public void processRoleRequest(int requestId, String status) {
        log.info("Processing role request ID {} with status {}", requestId, status);
        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException("Role request not found with ID: " + requestId));
        
        request.setStatus(status.toUpperCase());
        roleRequestRepository.save(request);

        if ("APPROVED".equalsIgnoreCase(status)) {
            updateUserRole(request.getUser().getUserId(), request.getRequestedRole());
        }

        // Notify user
        try {
            java.util.Map<String, Object> notification = new java.util.HashMap<>();
            notification.put("recipientId", request.getUser().getUserId());
            notification.put("actorId", 0);
            notification.put("type", "SYSTEM");
            notification.put("message", "Your role request for " + request.getRequestedRole() + " has been " + status.toLowerCase() + ".");
            notification.put("relatedId", request.getRequestId());
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.error("Failed to notify user about role request update: ", e);
        }
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        String avatar = user.getProfileImageUrl();
        if (avatar != null && (avatar.startsWith("http://localhost:8080/") || avatar.startsWith("http://localhost:8081/"))) {
            avatar = avatar.replace("http://localhost:8080/", gatewayUrl + "/").replace("http://localhost:8081/", gatewayUrl + "/");
        }
        return new UserResponseDTO(
            user.getUserId(), 
            user.getUsername(), 
            user.getEmail(), 
            user.getRole(),
            user.getFullName(), 
            avatar,
            user.getBio(),
            user.getAge(),
            user.getSubscriptionStartDate(),
            user.getSubscriptionEndDate()
        );
    }
    
    
}