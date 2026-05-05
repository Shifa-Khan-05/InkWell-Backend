package com.commentservice.controller;

import com.commentservice.dto.CommentResponseDTO;
import com.commentservice.entity.Comment;
import com.commentservice.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentResourceTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentResource commentResource;

    private Comment comment;
    private CommentResponseDTO commentResponseDTO;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setCommentId(1L);
        comment.setContent("Test comment");

        commentResponseDTO = new CommentResponseDTO();
        commentResponseDTO.setCommentId(1L);
        commentResponseDTO.setContent("Test comment");
    }

    @Test
    void addComment_Success() {
        when(commentService.addComment(any(Comment.class))).thenReturn(comment);

        ResponseEntity<Comment> response = commentResource.addComment(comment);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(comment);
    }

    @Test
    void getByPost_Success() {
        when(commentService.getCommentsByPost(1)).thenReturn(List.of(commentResponseDTO));

        ResponseEntity<List<CommentResponseDTO>> response = commentResource.getByPost(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getPending_Success() {
        when(commentService.getPendingComments()).thenReturn(List.of(commentResponseDTO));

        ResponseEntity<List<CommentResponseDTO>> response = commentResource.getPending();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void approve_Success() {
        doNothing().when(commentService).approveComment(1L);

        ResponseEntity<Void> response = commentResource.approve(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(commentService).approveComment(1L);
    }

    @Test
    void reject_Success() {
        doNothing().when(commentService).rejectComment(1L);

        ResponseEntity<Void> response = commentResource.reject(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(commentService).rejectComment(1L);
    }

    @Test
    void like_Success() {
        doNothing().when(commentService).likeComment(1L);

        ResponseEntity<Void> response = commentResource.like(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(commentService).likeComment(1L);
    }

    @Test
    void getReplies_Success() {
        when(commentService.getReplies(1L)).thenReturn(List.of(comment));

        ResponseEntity<List<Comment>> response = commentResource.getReplies(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void delete_Success() {
        doNothing().when(commentService).deleteComment(1L);

        ResponseEntity<Void> response = commentResource.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(commentService).deleteComment(1L);
    }
}
