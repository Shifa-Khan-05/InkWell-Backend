package com.commentservice.repository;

import com.commentservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByPostIdOrderByCreatedAtDesc(Integer postId);

	List<Comment> findByParentId(Long parentId);
}