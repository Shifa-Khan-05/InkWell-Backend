package com.newsletterservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
@Data
public class Subscriber {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String email;

	private LocalDateTime subscribedAt;

	private boolean active = true;

	@PrePersist
	protected void onCreate() {
		subscribedAt = LocalDateTime.now();
	}

	// Inside Subscriber.java
	@Column(nullable = false)
	private String status = "PENDING"; // PENDING, ACTIVE

	@Column(unique = true)
	private String verificationToken;
}