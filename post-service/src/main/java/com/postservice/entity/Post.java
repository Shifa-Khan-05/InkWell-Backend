package com.postservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Data // ✅ This handles all Getters, Setters, ToString, and Equals
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int postId;

    @Column(nullable = false)
    private int authorId; 

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug; 

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content; 

    private String excerpt;
    @Column(length = 1000)
    private String featuredImageUrl;
    private String status; 

    // ✅ This field MUST exist for setCategoryId() to work
    @Column(name = "category_id")
    private Integer categoryId;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag_id")
    private List<Integer> tagIds;

    private int readTimeMin;
    private int viewCount = 0;
    
    @Column(name = "likes_count")
    private int likesCount = 0; 

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}