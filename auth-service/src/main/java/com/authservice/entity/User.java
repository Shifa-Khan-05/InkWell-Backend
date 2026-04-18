package com.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * User Entity
 * This represents the user in our database.
 * I'm using Lombok @Data to avoid writing getters and setters manually.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId; //

    @Column(unique = true, nullable = false)
    private String username; // 

    @Column(unique = true, nullable = false)
    private String email; // 

    @Column(nullable = false)
    private String passwordHash; // We must hash passwords!

    private String fullName; // 
    
    private String role; // READER, AUTHOR, or ADMIN 
    
    private String bio; //
    
    private String avatarUrl; // 
    
    private boolean isActive = true; //

    private LocalDateTime createdAt = LocalDateTime.now(); // 
}