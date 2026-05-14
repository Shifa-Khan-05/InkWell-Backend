package com.commentservice.service;

import com.commentservice.dto.CommentResponseDTO;
import com.commentservice.entity.Comment;
import java.util.List;

public interface CommentService {
    Comment addComment(Comment comment);

    // Fetch approved comments for a specific post
    List<CommentResponseDTO> getCommentsByPost(Integer postId);

    // Fetch all pending comments for Admin Moderation
    List<CommentResponseDTO> getPendingComments();

    List<Comment> getReplies(Long parentId);

    void approveComment(Long commentId);

    void rejectComment(Long commentId);

    void likeComment(Long commentId);

    void deleteComment(Long commentId, int userId);
}