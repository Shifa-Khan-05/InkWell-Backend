package com.commentservice.service;

import com.commentservice.client.AuthClient;
import com.commentservice.dto.CommentResponseDTO;
import com.commentservice.dto.UserResponseDTO;
import com.commentservice.entity.Comment;
import com.commentservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final AuthClient authClient;

	@Override
	public Comment addComment(Comment comment) {
		comment.setStatus("PENDING");
		return commentRepository.save(comment);
	}

	private CommentResponseDTO mapToDTO(Comment comment) {
		CommentResponseDTO dto = new CommentResponseDTO();
		dto.setCommentId(comment.getCommentId());
		dto.setContent(comment.getContent());
		dto.setStatus(comment.getStatus());
		dto.setLikesCount(comment.getLikesCount());
		dto.setCreatedAt(comment.getCreatedAt());

		// Fetch name from Auth-Service via Feign
		try {
			UserResponseDTO user = authClient.getUserById(comment.getUserId());
			dto.setAuthorName(user.getFullName());
		} catch (Exception e) {
			dto.setAuthorName("InkWell Reader");
		}
		return dto;
	}

	@Override
	public List<Comment> getReplies(Long parentId) {
		return commentRepository.findByParentId(parentId);
	}

	@Override
	@Transactional
	public void approveComment(Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));
		comment.setStatus("APPROVED");
		commentRepository.save(comment);
	}

	@Override
	@Transactional
	public void rejectComment(Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));
		comment.setStatus("REJECTED");
		commentRepository.save(comment);
	}

	@Override
	@Transactional
	public void likeComment(Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));
		comment.setLikesCount(comment.getLikesCount() + 1);
		commentRepository.save(comment);
	}

	@Override
	public void deleteComment(Long commentId) {
		commentRepository.deleteById(commentId);
	}

	@Override
	public List<CommentResponseDTO> getCommentsByPost(Integer postId) {
		// Fetch only APPROVED comments for the post detail view
		return commentRepository.findByPostIdOrderByCreatedAtDesc(postId).stream()
				.filter(c -> "APPROVED".equals(c.getStatus())).map(this::mapToDTO).collect(Collectors.toList());
	}

	@Override
	public List<CommentResponseDTO> getPendingComments() {
		// Fetch all PENDING comments for the Admin Dashboard
		return commentRepository.findAll().stream().filter(c -> "PENDING".equals(c.getStatus())).map(this::mapToDTO)
				.collect(Collectors.toList());
	}
}