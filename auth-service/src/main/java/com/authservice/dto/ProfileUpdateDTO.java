package com.authservice.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String fullName;
    private String bio;
    private String profileImageUrl;
    @Column(name = "age")
    private Integer age; // Add this field
}