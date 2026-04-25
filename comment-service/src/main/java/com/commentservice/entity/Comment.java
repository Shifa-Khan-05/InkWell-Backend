package com.commentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private Integer postId;
    private Integer userId;
    
    private Integer authorId;

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