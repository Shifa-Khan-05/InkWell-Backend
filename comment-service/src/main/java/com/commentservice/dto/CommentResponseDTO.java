package com.commentservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponseDTO {
    private Long commentId;
    private String content;
    private String authorName; // Fetched via AuthClient
    private String status;
    private int likesCount;
    private LocalDateTime createdAt;
}