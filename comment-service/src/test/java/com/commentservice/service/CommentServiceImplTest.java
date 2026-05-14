package com.commentservice.service;

import com.commentservice.client.AuthClient;
import com.commentservice.client.NotificationClient;
import com.commentservice.dto.CommentResponseDTO;
import com.commentservice.dto.UserResponseDTO;
import com.commentservice.entity.Comment;
import com.commentservice.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AuthClient authClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setCommentId(1L);
        comment.setPostId(1);
        comment.setUserId(1);
        comment.setAuthorId(2);
        comment.setContent("Test Content");
        comment.setStatus("PENDING");
        comment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getReplies_Success() {
        when(commentRepository.findByParentId(1L)).thenReturn(List.of(comment));
        assertThat(commentService.getReplies(1L)).hasSize(1);
    }

    @Test
    void rejectComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        commentService.rejectComment(1L);
        assertThat(comment.getStatus()).isEqualTo("REJECTED");
        verify(commentRepository).save(comment);
    }

    @Test
    void likeComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        commentService.likeComment(1L);
        assertThat(comment.getLikesCount()).isEqualTo(1);
        verify(commentRepository).save(comment);
    }

    @Test
    void deleteComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        UserResponseDTO user = new UserResponseDTO();
        user.setRole("ROLE_READER");
        when(authClient.getUserById(1)).thenReturn(user);

        commentService.deleteComment(1L, 1);
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void getCommentsByPost_Success() {
        comment.setStatus("APPROVED");
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(1)).thenReturn(List.of(comment));
        
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setFullName("Test User");
        when(authClient.getUserById(1)).thenReturn(userResponse);

        List<CommentResponseDTO> result = commentService.getCommentsByPost(1);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthorName()).isEqualTo("Test User");
    }

    @Test
    void getPendingComments_Success() {
        when(commentRepository.findAll()).thenReturn(List.of(comment));
        when(authClient.getUserById(1)).thenThrow(new RuntimeException("Fallback"));

        List<CommentResponseDTO> result = commentService.getPendingComments();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthorName()).isEqualTo("InkWell Reader");
    }

    @Test
    void approveComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        
        commentService.approveComment(1L);
        
        assertThat(comment.getStatus()).isEqualTo("APPROVED");
        verify(commentRepository).save(comment);
        verify(notificationClient, times(2)).sendNotification(any());
    }

    @Test
    void addComment_Success() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        
        Comment result = commentService.addComment(comment);
        
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(notificationClient).sendNotification(any());
    }
}
