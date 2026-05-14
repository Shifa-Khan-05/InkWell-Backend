package com.mediaservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mediaId;

    private Integer uploaderId; // User ID from Auth-Service
    private String filename;
    private String originalName;
    
    @Column(columnDefinition = "TEXT")
    private String url; // The public URL to access the image
    
    private String mimeType; // e.g. image/jpeg, image/png
    private Long sizeKb;
    private String altText; // For SEO and Accessibility
    
    private Integer linkedPostId; // Optional: Link to a specific post
    
    private boolean isDeleted = false; // Soft-delete flag
    
    private LocalDateTime uploadedAt;

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}