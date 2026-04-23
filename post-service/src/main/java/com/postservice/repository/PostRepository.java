package com.postservice.repository;

import com.postservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

	Optional<Post> findBySlug(String slug);

	List<Post> findAllByStatus(String status);

	List<Post> findByAuthorId(int authorId);

	@Modifying
	@Transactional
	@Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :id")
	void incrementViewCount(@Param("id") int id);

	@Modifying
	@Transactional
	@Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.postId = :id")
	void incrementLikes(@Param("id") int id);

	List<Post> findByStatusOrderByCreatedAtDesc(String status);

	
	List<Post> findByCategoryId(Integer categoryId);

	boolean existsBySlug(String baseSlug);
}