package com.postservice.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private int userId;
    private String fullName;
    private String email;
    private String role;
}