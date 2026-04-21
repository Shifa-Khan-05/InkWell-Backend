package com.authservice.service;

import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;

	// ================= REGISTER =================
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
	    
	    // ✅ FIX: Dynamically set role and add "ROLE_" prefix
	    String selectedRole = regDto.getRole() != null ? regDto.getRole() : "READER";
	    user.setRole("ROLE_" + selectedRole.toUpperCase());
	    
	    user.setActive(true);

	    User savedUser = userRepository.save(user);
	    return mapToResponseDTO(savedUser);
	}

	// ================= LOGIN =================
	@Override
	public String login(String email, String password) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));

		if (!user.isActive()) {
			throw new RuntimeException("Account is suspended!");
		}

		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new RuntimeException("Invalid credentials!");
		}

		return jwtUtils.generateToken(user.getEmail());
	}

	// ================= GET USER BY ID =================
	@Override
	public UserResponseDTO getUserById(int userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
		return mapToResponseDTO(user);
	}

	// ================= GET USER BY EMAIL =================
	@Override
	public UserResponseDTO findByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found with email: " + email));
		return mapToResponseDTO(user);
	}

	// ================= GET ROLE =================
	@Override
	public String getRoleByEmail(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
		return user.getRole();
	}

	// ================= UPDATE PROFILE (JSON) =================
	@Override
	public UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		if (updateDto.getFullName() != null)
			user.setFullName(updateDto.getFullName());
		if (updateDto.getBio() != null)
			user.setBio(updateDto.getBio());
		if (updateDto.getProfileImageUrl() != null)
			user.setProfileImageUrl(updateDto.getProfileImageUrl());

		User updatedUser = userRepository.save(user);
		return mapToResponseDTO(updatedUser);
	}

	// ================= UPDATE PROFILE WITH IMAGE (BROWSE FILES) =================
	@Override
	public UserResponseDTO updateProfileWithFile(int userId, String fullName, String bio, MultipartFile image) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));

		if (fullName != null && !fullName.isEmpty())
			user.setFullName(fullName);
		
		// Bio can be empty, so we just check if it's null
		if (bio != null)
			user.setBio(bio);

		if (image != null && !image.isEmpty()) {
			try {
				// 1. Define and Create Directory
				String uploadDir = "uploads/";
				Path uploadPath = Paths.get(uploadDir);
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				// 2. Get File Extension
				String originalFileName = image.getOriginalFilename();
				String extension = ".jpg"; // Default
				if (originalFileName != null && originalFileName.contains(".")) {
					extension = originalFileName.substring(originalFileName.lastIndexOf("."));
				}

				// 3. Generate Unique Filename
				String fileName = "user_" + userId + "_" + System.currentTimeMillis() + extension;
				Path filePath = uploadPath.resolve(fileName);

				// 4. Save File to Disk
				Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				// 5. Store Standardized URL format for Frontend access
				String fullImageUrl = "http://localhost:8081/uploads/" + fileName;
				user.setProfileImageUrl(fullImageUrl);

			} catch (IOException e) {
				throw new RuntimeException("File storage failed: " + e.getMessage());
			}
		}

		User updatedUser = userRepository.save(user);
		return mapToResponseDTO(updatedUser);
	}

	// ================= OAUTH LOGIN =================
	@Override
	public String processOAuthPostLogin(String email, String name, String provider) {
		User user = userRepository.findByEmail(email).orElseGet(() -> {
			User newUser = new User();
			newUser.setEmail(email);
			newUser.setFullName(name);
			newUser.setUsername(email.split("@")[0] + "_" + provider.toLowerCase());
			newUser.setRole("ROLE_READER");
			newUser.setActive(true);
			newUser.setPasswordHash("OAUTH_USER");
			return userRepository.save(newUser);
		});

		return jwtUtils.generateToken(user.getEmail());
	}

	// ================= DEACTIVATE =================
	@Override
	public void deactivateAccount(int userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
		user.setActive(false);
		userRepository.save(user);
	}

	// ================= MANUAL MAPPER (NO MODEL MAPPER) =================
	private UserResponseDTO mapToResponseDTO(User user) {
		return new UserResponseDTO(
				user.getUserId(), 
				user.getUsername(), 
				user.getEmail(), 
				user.getRole(),
				user.getFullName(), 
				user.getProfileImageUrl()
		);
	}
}