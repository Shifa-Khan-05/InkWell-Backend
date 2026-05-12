package com.authservice.service;

import org.springframework.web.multipart.MultipartFile;
import com.authservice.dto.ProfileUpdateDTO;
import com.authservice.dto.UserRegistrationDTO;
import com.authservice.dto.UserResponseDTO;
import com.authservice.entity.User;

public interface AuthService {
	void sendRegistrationOtp(String email);

	UserResponseDTO register(UserRegistrationDTO regDto);

	String login(String email, String password);

	UserResponseDTO getUserById(int userId);

	UserResponseDTO findByEmail(String email);

	UserResponseDTO updateProfileWithFile(int userId, String fullName, String username, String bio, Integer age,
			String password, MultipartFile image);

	String processOAuthPostLogin(String email, String name, String provider);

	void deactivateAccount(int userId);

	UserResponseDTO updateProfile(int userId, ProfileUpdateDTO updateDto);

	String getRoleByEmail(String email);

	void updateUserRole(Integer userId, String newRole);

	void deleteUser(Integer userId);

	Integer getUserIdByEmail(String email);

	void upgradeToPremium(Integer userId);

	void resetUserPassword(User user, String newPassword);

	void requestRoleChange(int userId, String requestedRole);

	java.util.List<com.authservice.entity.RoleRequest> getAllRoleRequests();

	void processRoleRequest(int requestId, String status);
}