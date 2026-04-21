package com.postservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Post Entity Manages the lifecycle and metadata of a blog article.
 */
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int postId;

	@Column(nullable = false)
	private int authorId; // Linked to Auth-Service

	@Column(nullable = false)
	private String title;

	@Column(unique = true, nullable = false)
	private String slug; // SEO-friendly URL

	@Lob
	private String content; // Rich content

	private String excerpt;
	private String featuredImageUrl;

	private String status; // DRAFT, PUBLISHED, UNPUBLISHED, ARCHIVED

	private int readTimeMin;
	private int viewCount = 0;
	
	@Column(name = "likes_count")
	private int likesCount = 0; // ✅ Initialize to 0 to prevent null/NaN issues

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime publishedAt;

	// Automatically set timestamps
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	
	private String imageUrl; // ✅ Added to store the image path
}