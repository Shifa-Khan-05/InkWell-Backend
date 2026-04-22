package com.commentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private Integer postId;
    private Integer userId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    private Long parentId; // 0 or null for top-level comments
    
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    private int likesCount = 0;
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}