package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor 
public class UserRegistrationDTO {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role; 
    private String otp;
}