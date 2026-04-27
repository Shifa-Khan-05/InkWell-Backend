package com.websitecontroller.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommentResponseDTO implements Serializable {
    private Long commentId;
    private String content;
    private String authorName; // Enriched from Auth
    private String status;
    private int likesCount;
    private LocalDateTime createdAt;
}