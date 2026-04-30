package com.websitecontroller.controller;

import com.websitecontroller.client.CommentClient;
import com.websitecontroller.client.PostClient;
import com.websitecontroller.dto.CommentResponseDTO;
import com.websitecontroller.dto.PostResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostClient postClient;

    @Mock
    private CommentClient commentClient;

    @InjectMocks
    private BlogController blogController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(blogController).build();
    }

    @Test
    void testGetFullPostDetails_Success() throws Exception {
        String slug = "spring-boot-guide";
        int userId = 10;
        int postId = 55;

        // Mock Post
        PostResponseDTO mockPost = new PostResponseDTO();
        mockPost.setPostId(postId);
        mockPost.setTitle("Spring Boot Guide");
        mockPost.setSlug(slug);

        // Mock Comment
        CommentResponseDTO mockComment = new CommentResponseDTO();
        mockComment.setCommentId(1L);
        mockComment.setContent("Great article!");

        // Define Behavior
        when(postClient.getPostBySlug(slug, userId)).thenReturn(mockPost);
        when(commentClient.getCommentsByPost(postId)).thenReturn(List.of(mockComment));

        // Execute and Verify
        mockMvc.perform(get("/api/view/post/{slug}", slug)
                .param("userId", String.valueOf(userId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.title", is("Spring Boot Guide")))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].content", is("Great article!")));
    }
}