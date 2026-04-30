package com.authservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private int userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String profileImageUrl;
    private String bio;
    private Integer age;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
}