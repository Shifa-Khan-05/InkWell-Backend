package com.postservice.controller;

import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostResourceTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostResource postResource;

    private PostResponseDTO postResponseDTO;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        postResponseDTO = new PostResponseDTO();
        postResponseDTO.setPostId(1);
        postResponseDTO.setTitle("Test Title");

        mockFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
    }

    @Test
    void createPost_Success() throws IOException {
        when(postService.createPostWithImage(any(PostCreationDTO.class), eq(mockFile))).thenReturn(postResponseDTO);
        PostCreationDTO dto = new PostCreationDTO();
        dto.setTitle("Test Title");
        dto.setContent("Content");
        dto.setAuthorId(1);

        ResponseEntity<PostResponseDTO> response = postResource.createPost(dto, mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(postResponseDTO);
    }

    @Test
    void createPost_WithNullOptionals_Success() throws IOException {
        when(postService.createPostWithImage(any(PostCreationDTO.class), isNull())).thenReturn(postResponseDTO);
        PostCreationDTO dto = new PostCreationDTO();
        dto.setTitle("Test Title");

        ResponseEntity<PostResponseDTO> response = postResource.createPost(dto, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(postResponseDTO);
    }

    @Test
    void updatePost_Success() throws IOException {
        when(postService.updatePost(eq(1), any(PostCreationDTO.class), eq(mockFile))).thenReturn(postResponseDTO);
        PostCreationDTO dto = new PostCreationDTO();
        dto.setTitle("Updated Title");

        ResponseEntity<PostResponseDTO> response = postResource.updatePost(1, dto, mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponseDTO);
    }

    @Test
    void updatePost_WithNullOptionals_Success() throws IOException {
        when(postService.updatePost(eq(1), any(PostCreationDTO.class), isNull())).thenReturn(postResponseDTO);
        PostCreationDTO dto = new PostCreationDTO();
        dto.setTitle("Updated Title");

        ResponseEntity<PostResponseDTO> response = postResource.updatePost(1, dto, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponseDTO);
    }

    @Test
    void getPublishedPosts_Success() {
        when(postService.getPublishedPosts()).thenReturn(List.of(postResponseDTO));

        ResponseEntity<List<PostResponseDTO>> response = postResource.getPublishedPosts();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getPostBySlug_Success() {
        when(postService.getPostBySlug("test-title", 1)).thenReturn(postResponseDTO);

        ResponseEntity<PostResponseDTO> response = postResource.getPostBySlug("test-title", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponseDTO);
    }

    @Test
    void getByAuthor_Success() {
        when(postService.getPostsByAuthor(1)).thenReturn(List.of(postResponseDTO));

        ResponseEntity<List<PostResponseDTO>> response = postResource.getByAuthor(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getById_Success() {
        when(postService.getPostById(1)).thenReturn(postResponseDTO);

        ResponseEntity<PostResponseDTO> response = postResource.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponseDTO);
    }

    @Test
    void getByCategoryId_Success() {
        when(postService.getPostsByCategoryId(1)).thenReturn(List.of(postResponseDTO));

        ResponseEntity<List<PostResponseDTO>> response = postResource.getByCategoryId(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void incrementLikes_Success() {
        doNothing().when(postService).incrementLikes(1, 1);

        ResponseEntity<Void> response = postResource.incrementLikes(1, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postService).incrementLikes(1, 1);
    }

    @Test
    void deletePost_Success() {
        doNothing().when(postService).deletePost(1);

        ResponseEntity<Void> response = postResource.deletePost(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(postService).deletePost(1);
    }

    @Test
    void toggleSavePost_Success() {
        doNothing().when(postService).toggleSavePost(1, 1);

        ResponseEntity<Void> response = postResource.toggleSavePost(1, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postService).toggleSavePost(1, 1);
    }

    @Test
    void getSavedPosts_Success() {
        when(postService.getSavedPostsByUser(1)).thenReturn(List.of(postResponseDTO));

        ResponseEntity<List<PostResponseDTO>> response = postResource.getSavedPosts(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void isPostSaved_Success() {
        when(postService.isPostSavedByUser(1, 1)).thenReturn(true);

        ResponseEntity<Boolean> response = postResource.isPostSaved(1, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }
}
