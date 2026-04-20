package com.postservice.repository;

import com.postservice.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * ✅ Checks if a specific user has already liked a specific post.
     * Used to prevent duplicate likes and to set the 'liked' state on the frontend.
     */
    boolean existsByPostIdAndUserId(int postId, int userId);

    /**
     * Optional: Finds a specific like record. 
     * Useful if you want to implement an "Unlike" feature later.
     */
    Optional<PostLike> findByPostIdAndUserId(int postId, int userId);

    /**
     * Optional: Removes a like record.
     */
    void deleteByPostIdAndUserId(int postId, int userId);
    
}