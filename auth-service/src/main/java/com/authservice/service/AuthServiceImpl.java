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
		String selectedRole = regDto.getRole() != null ? regDto.getRole() : "READER";
		user.setRole("ROLE_" + selectedRole.toUpperCase());
		user.setActive(true);
		return mapToResponseDTO(userRepository.save(user));
	}

	@Override
	public String login(String email, String password) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
		if (!user.isActive())
			throw new RuntimeException("Account is suspended!");
		if (!passwordEncoder.matches(password, user.getPasswordHash()))
			throw new RuntimeException("Invalid credentials!");
		return jwtUtils.generateToken(user.getEmail());
	}

	@Override
	public UserResponseDTO getUserById(int userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
		return mapToResponseDTO(user);
	}

	@Override
	public UserResponseDTO findByEmail(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
		return mapToResponseDTO(user);
	}

	@Override
	public String getRoleByEmail(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
		return user.getRole();
	}

	@Override
	public UserResponseDTO updateProfileWithFile(int userId, String fullName, String bio, MultipartFile image) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
		if (fullName != null && !fullName.isEmpty())
			user.setFullName(fullName);
		if (bio != null)
			user.setBio(bio);

		if (image != null && !image.isEmpty()) {
			try {
				String uploadDir = "uploads/";
				Path uploadPath = Paths.get(uploadDir);
				if (!Files.exists(uploadPath))
					Files.createDirectories(uploadPath);
				String fileName = "user_" + userId + "_" + System.currentTimeMillis() + ".jpg";
				Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
				user.setProfileImageUrl("http://localhost:8081/uploads/" + fileName);
			} catch (IOException e) {
				throw new RuntimeException("File storage failed");
			}
		}
		return mapToResponseDTO(userRepository.save(user));
	}

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

	@Override
	public void deactivateAccount(int userId) {
		User user = userRepository.findById(userId).orElseThrow();
		user.setActive(false);
		userRepository.save(user);
	}

	@Override
	public UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto) {
		User user = userRepository.findById(userId).orElseThrow();
		if (updateDto.getFullName() != null)
			user.setFullName(updateDto.getFullName());
		if (updateDto.getBio() != null)
			user.setBio(updateDto.getBio());
		return mapToResponseDTO(userRepository.save(user));
	}

	private UserResponseDTO mapToResponseDTO(User user) {
		return new UserResponseDTO(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole(),
				user.getFullName(), user.getProfileImageUrl());
	}

	@Override
	public void updateUserRole(Integer userId, String newRole) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Identity not found"));

		// ✅ Logic: Ensure it becomes ROLE_ADMIN, ROLE_AUTHOR, or ROLE_READER
		String formattedRole = newRole.toUpperCase();
		if (!formattedRole.startsWith("ROLE_")) {
			formattedRole = "ROLE_" + formattedRole;
		}

		user.setRole(formattedRole);
		userRepository.save(user);
	}

	@Override
	public void deleteUser(Integer userId) {
		if (!userRepository.existsById(userId)) {
			throw new RuntimeException("Identity does not exist");
		}
		userRepository.deleteById(userId);
	}
	
	public Integer getUserIdByEmail(String email) {
	    return userRepository.findByEmail(email)
	            .map(User::getUserId) // Or .getId() depending on your entity
	            .orElse(null);
	}
	
	// Add this method to AuthServiceImpl.java
	@Override
	public void upgradeToPremium(Integer userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
	    
	    // ✅ Change role to ROLE_PREMIUM
	    user.setRole("ROLE_PREMIUM");
	    userRepository.save(user);
	    
	    System.out.println("User " + userId + " has been upgraded to PREMIUM successfully.");
	}
}