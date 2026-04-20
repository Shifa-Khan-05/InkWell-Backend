package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
	private int userId;
	private String username;
	private String email;
	private String role;
	private String fullName;
	private String profileImageUrl;
	
}