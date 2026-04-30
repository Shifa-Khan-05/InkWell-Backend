package com.postservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer postId;

    private LocalDateTime savedAt = LocalDateTime.now();

    public SavedPost(Integer userId, Integer postId) {
        this.userId = userId;
        this.postId = postId;
        this.savedAt = LocalDateTime.now();
    }
}
