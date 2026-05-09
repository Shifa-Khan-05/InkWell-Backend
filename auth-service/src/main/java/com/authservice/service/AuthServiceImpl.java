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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @org.springframework.beans.factory.annotation.Value("${gateway.url:https://3.108.190.193.nip.io}")
    private String gatewayUrl;

    @org.springframework.beans.factory.annotation.Value("${FRONTEND_URL:https://inkwell-blogging.netlify.app}")
    private String frontendUrl;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.amazonaws.services.s3.AmazonS3 s3Client;

    @org.springframework.beans.factory.annotation.Value("${AWS_S3_BUCKET:inkwell-media-storage}")
    private String bucketName;

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

        String otp = String.format("%06d", new Random().nextInt(999999));
        
        EmailOtp emailOtp = emailOtpRepository.findByEmail(email).orElse(new EmailOtp());
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setCreatedAt(LocalDateTime.now());
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailOtpRepository.save(emailOtp);

        Map<String, String> mailData = new HashMap<>();
        mailData.put("recipientEmail", email);
        mailData.put("subject", "Email Verification OTP");
        mailData.put("title", "Verify Your Email");
        mailData.put("body", "Your OTP for InkWell registration is: " + otp + ". It expires in 10 minutes.");
        mailData.put("actionUrl", frontendUrl + "/register");
        notificationClient.sendStyledEmail(mailData);
    }

    @Override
    public UserResponseDTO register(UserRegistrationDTO regDto) {
        if (userRepository.existsByEmail(regDto.getEmail())) {
            throw new com.authservice.exception.BadRequestException("Email already registered!");
        }

        EmailOtp emailOtp = emailOtpRepository.findByEmail(regDto.getEmail())
                .orElseThrow(() -> new com.authservice.exception.BadRequestException("No OTP found. Please request a new one."));

        if (!emailOtp.getOtp().equals(regDto.getOtp())) {
            throw new com.authservice.exception.BadRequestException("Invalid OTP!");
        }

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new com.authservice.exception.BadRequestException("OTP has expired.");
        }

        User user = new User();
        user.setUsername(regDto.getUsername());
        user.setEmail(regDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(regDto.getPassword()));
        user.setFullName(regDto.getFullName());
        
        String selectedRole = regDto.getRole() != null ? regDto.getRole() : "READER";
        user.setRole(ROLE_PREFIX + selectedRole.toUpperCase());
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        emailOtpRepository.delete(emailOtp);

        log.info("User registered successfully: {}", savedUser.getEmail());
        return mapToResponseDTO(savedUser);
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        
        if (!user.isActive()) {
            throw new com.authservice.exception.AuthException("Account is suspended!");
        }
            
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new com.authservice.exception.AuthException("Invalid credentials!");
        }
            
        return jwtUtils.generateToken(user.getEmail());
    }

    @Override
    public UserResponseDTO updateProfileWithFile(int userId, String fullName, String username, String bio, Integer age, String password, MultipartFile image) {
        log.info("Updating profile with file for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
                
        if (fullName != null && !fullName.isEmpty()) user.setFullName(fullName);
        if (username != null && !username.isEmpty()) user.setUsername(username);
        if (bio != null) user.setBio(bio);
        if (age != null) user.setAge(age);
        if (password != null && !password.isEmpty()) user.setPasswordHash(passwordEncoder.encode(password));

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = "user_" + userId + "_" + System.currentTimeMillis() + ".jpg";
                boolean s3Success = false;

                if (s3Client != null && bucketName != null && !bucketName.isEmpty()) {
                    try {
                        com.amazonaws.services.s3.model.ObjectMetadata metadata = new com.amazonaws.services.s3.model.ObjectMetadata();
                        metadata.setContentLength(image.getSize());
                        metadata.setContentType(image.getContentType());
                        s3Client.putObject(new com.amazonaws.services.s3.model.PutObjectRequest(bucketName, "uploads/" + fileName, image.getInputStream(), metadata)
                            .withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead));
                        user.setProfileImageUrl(s3Client.getUrl(bucketName, "uploads/" + fileName).toString());
                        s3Success = true;
                    } catch (Exception s3Ex) {
                        log.warn("S3 upload failed, falling back to local storage: {}", s3Ex.getMessage());
                    }
                }

                if (!s3Success) {
                    Path uploadPath = Paths.get("uploads").toAbsolutePath();
                    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                    
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    String baseUrl = gatewayUrl;
                    if (baseUrl != null && baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    user.setProfileImageUrl(baseUrl + "/uploads/" + fileName);
                }
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new com.authservice.exception.BadRequestException("File storage failed: " + e.getMessage());
            }
        }
        return mapToResponseDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO getUserById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        return mapToResponseDTO(user);
    }

    @Override
    public String getRoleByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        return user.getRole();
    }

    @Override
    public String processOAuthPostLogin(String email, String name, String provider) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
                
        if (updateDto.getFullName() != null) user.setFullName(updateDto.getFullName());
        if (updateDto.getBio() != null) user.setBio(updateDto.getBio());
        if (updateDto.getAge() != null) user.setAge(updateDto.getAge());
            
        return mapToResponseDTO(userRepository.save(user));
    }

    @Override
    public void updateUserRole(Integer userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(IDENTITY_NOT_FOUND));

        String formattedRole = newRole.toUpperCase();
        if (!formattedRole.startsWith(ROLE_PREFIX)) formattedRole = ROLE_PREFIX + formattedRole;

        user.setRole(formattedRole);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new com.authservice.exception.ResourceNotFoundException(IDENTITY_NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }
    
    @Override
    public Integer getUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getUserId).orElse(null);
    }
    
    @Override
    public void upgradeToPremium(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        
        user.setRole(ROLE_PREFIX + "PREMIUM");
        user.setMembershipLevel("PREMIUM");
        user.setSubscriptionStartDate(LocalDateTime.now());
        user.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
        userRepository.save(user);
    }

    @Override
    public void resetUserPassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public void requestRoleChange(int userId, String requestedRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException(USER_NOT_FOUND));
        
        if (roleRequestRepository.existsByUserUserIdAndStatus(userId, "PENDING")) {
            throw new com.authservice.exception.BadRequestException("A request is already pending.");
        }

        RoleRequest request = RoleRequest.builder()
                .user(user)
                .requestedRole(requestedRole)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build();
        roleRequestRepository.save(request);
    }

    @Override
    public List<RoleRequest> getAllRoleRequests() {
        return roleRequestRepository.findAll();
    }

    @Override
    public void processRoleRequest(int requestId, String status) {
        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new com.authservice.exception.ResourceNotFoundException("Request not found"));
        
        request.setStatus(status.toUpperCase());
        roleRequestRepository.save(request);

        if ("APPROVED".equalsIgnoreCase(status)) {
            updateUserRole(request.getUser().getUserId(), request.getRequestedRole());
        }
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        String avatar = user.getProfileImageUrl();
        if (avatar != null && (avatar.startsWith("http://localhost:8080/") || avatar.startsWith("http://localhost:8081/"))) {
            avatar = avatar.replace("http://localhost:8080/", gatewayUrl + "/").replace("http://localhost:8081/", gatewayUrl + "/");
        }
        return new UserResponseDTO(
            user.getUserId(), user.getUsername(), user.getEmail(), user.getRole(),
            user.getFullName(), avatar, user.getBio(), user.getAge(),
            user.getSubscriptionStartDate(), user.getSubscriptionEndDate()
        );
    }
}