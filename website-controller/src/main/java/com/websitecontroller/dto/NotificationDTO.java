package com.websitecontroller.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotificationDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer recipientId;
    private Integer actorId;
    private String type;        // e.g., "LIKE", "COMMENT", "POST_CREATED"
    private String message;     // The alert text
    private Integer relatedId;  // The ID of the Post or Comment involved
    private boolean isRead;
    private LocalDateTime createdAt;
}