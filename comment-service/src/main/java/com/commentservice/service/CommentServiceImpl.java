package com.commentservice.service;

import com.commentservice.client.AuthClient;
import com.commentservice.client.NotificationClient; // ✅ Added
import com.commentservice.dto.CommentResponseDTO;
import com.commentservice.dto.UserResponseDTO;
import com.commentservice.entity.Comment;
import com.commentservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final AuthClient authClient;
    private final NotificationClient notificationClient; // ✅ Injected

    @Override
    public List<Comment> getReplies(Long parentId) {
        return commentRepository.findByParentId(parentId);
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
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId).stream()
                .filter(c -> "APPROVED".equals(c.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentResponseDTO> getPendingComments() {
        return commentRepository.findAll().stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CommentResponseDTO mapToDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setContent(comment.getContent());
        dto.setStatus(comment.getStatus());
        dto.setLikesCount(comment.getLikesCount());
        dto.setCreatedAt(comment.getCreatedAt());

        try {
            UserResponseDTO user = authClient.getUserById(comment.getUserId());
            dto.setAuthorName(user.getFullName());
        } catch (Exception e) {
            dto.setAuthorName("InkWell Reader");
        }
        return dto;
    }
    
    @Override
    @Transactional
    public void approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        comment.setStatus("APPROVED");
        commentRepository.save(comment);

        try {
            Map<String, Object> note = new HashMap<>();
            // ✅ Corrected: Notify the Author of the post, not the commenter
            note.put("recipientId", comment.getAuthorId()); 
            note.put("actorId", 0); 
            note.put("type", "NEW_COMMENT");
            note.put("message", "Your narrative has a new verified discussion!");
            note.put("relatedId", comment.getPostId());

            notificationClient.sendNotification(note);
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
    }
    @Override
    public Comment addComment(Comment comment) {
        comment.setStatus("PENDING");
        Comment savedComment = commentRepository.save(comment);

        // ✨ Notify Author immediately that a discussion is pending
        Map<String, Object> note = new HashMap<>();
        note.put("recipientId", comment.getAuthorId()); 
        note.put("actorId", comment.getUserId());
        note.put("type", "NEW_COMMENT");
        note.put("message", "A new discussion is pending on your post.");
        note.put("relatedId", savedComment.getPostId());

        notificationClient.sendNotification(note);
        return savedComment;
    }
}