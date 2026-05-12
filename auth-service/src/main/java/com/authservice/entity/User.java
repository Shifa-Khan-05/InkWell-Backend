package com.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * User Entity This represents the user in our database. I'm using Lombok @Data
 * to avoid writing getters and setters manually.
 */
@Entity
@Table(name = "users")
@Data
@Getter
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

	private String provider; // LOCAL, GOOGLE, GITHUB

	private boolean isActive = true; //

	private LocalDateTime createdAt = LocalDateTime.now(); //

	private String profileImageUrl;

	@Column(name = "membership_level")
	private String membershipLevel = "FREE";

	@Column(name = "reset_token")
	private String resetToken;

	@Column(name = "token_expiry")
	private LocalDateTime tokenExpiry;
	
	@Column(name = "age")
	private Integer age; 

	@Column(name = "subscription_start_date")
	private LocalDateTime subscriptionStartDate;

	@Column(name = "subscription_end_date")
	private LocalDateTime subscriptionEndDate;
}