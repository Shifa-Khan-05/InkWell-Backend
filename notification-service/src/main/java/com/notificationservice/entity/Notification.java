package com.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long notificationId;

	private Integer recipientId; // The user receiving the alert
	private Integer actorId; // The user who triggered the alert (e.g., who liked the post)

	private String type; // NEW_COMMENT, LIKE, NEW_POST
	private String message;
	private Integer relatedId; // ID of the Post or Comment

	private boolean isRead = false;
	private LocalDateTime createdAt = LocalDateTime.now();
	
	private String relatedType; // "POST", "COMMENT", "USER"
}