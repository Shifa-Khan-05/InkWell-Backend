package com.commentservice.controller;

import com.commentservice.dto.CommentResponseDTO;

import com.commentservice.entity.Comment;
import com.commentservice.service.CommentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentResource {

	private final CommentService commentService;

	// ✅ Standardized to /create to match your test cases
	@PostMapping("/add")
	public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
		return new ResponseEntity<>(commentService.addComment(comment), HttpStatus.CREATED);
	}

	@GetMapping("/post/{postId}")
	public ResponseEntity<List<CommentResponseDTO>> getByPost(@PathVariable Integer postId) {
		return ResponseEntity.ok(commentService.getCommentsByPost(postId));
	}

	@GetMapping("/pending")
	public ResponseEntity<List<CommentResponseDTO>> getPending() {
		return ResponseEntity.ok(commentService.getPendingComments());
	}

	@PutMapping("/{id}/approve")
	public ResponseEntity<Void> approve(@PathVariable Long id) {
		commentService.approveComment(id);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/{id}/reject")
	public ResponseEntity<Void> reject(@PathVariable Long id) {
		commentService.rejectComment(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/like")
	public ResponseEntity<Void> like(@PathVariable Long id) {
		commentService.likeComment(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{parentId}/replies")
	public ResponseEntity<List<Comment>> getReplies(@PathVariable Long parentId) {
		return ResponseEntity.ok(commentService.getReplies(parentId));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam int userId) {
		commentService.deleteComment(id, userId);
		return ResponseEntity.noContent().build();
	}
}