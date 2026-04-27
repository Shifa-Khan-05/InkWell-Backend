package com.websitecontroller.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PostResponseDTO implements Serializable {
    private int postId;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String featuredImageUrl;
    private int authorId;
    private String fullName; // Enriched from Auth
    private Integer categoryId;
    private String status;
    private int readTimeMin;
    private int likesCount;
    private boolean isLikedByCurrentUser;
    private LocalDateTime createdAt;
}