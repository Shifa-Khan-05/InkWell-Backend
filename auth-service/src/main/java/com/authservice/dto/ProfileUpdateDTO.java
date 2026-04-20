package com.authservice.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String fullName;
    private String bio;
    private String profileImageUrl;
}