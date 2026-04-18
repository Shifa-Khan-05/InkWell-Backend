package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class UserRegistrationDTO {
	private String username;
	private String email;
	private String password; // Raw password from frontend
	private String fullName;
}