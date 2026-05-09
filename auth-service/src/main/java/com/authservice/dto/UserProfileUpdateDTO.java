package com.authservice.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String fullName;
    private String username;
    private String bio;
    private Integer age;
    private String password;
}
