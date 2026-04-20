package com.postservice.dto;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class PostResponseDTO {
    private int postId;
    private String title;
    private String content;
    private String slug;
    private String status;
    private int authorId;
    private String fullName; // Required for the Feign Client result
    private int readTimeMin;
    private int viewCount;
    private int likesCount;
    private boolean isLikedByCurrentUser; // ✅ ADD THIS
}