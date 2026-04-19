package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class UserResponseDTO {
	private int userId;
	private String username;
	private String email;
	private String role;
	private String fullName;
	
}