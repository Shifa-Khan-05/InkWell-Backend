package com.postservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"}) // ✅ Prevents duplicate likes
})
@Data
@NoArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private int postId;

    @Column(name = "user_id")
    private int userId;

    public PostLike(int postId, int userId) {
        this.postId = postId;
        this.userId = userId;
    }
}