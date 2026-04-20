package com.postservice.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.entity.Post;
import com.postservice.repository.PostRepository;
import com.postservice.client.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ModelMapper modelMapper;
    
    @Mock
    private AuthClient authClient; // Added since PostServiceImpl requires it

    @InjectMocks
    private PostServiceImpl postService;

    private PostCreationDTO creationDTO;
    private PostResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        creationDTO = new PostCreationDTO();
        creationDTO.setTitle("Test Blog Post");
        creationDTO.setContent("This is a test content.");
        creationDTO.setAuthorId(1);

        responseDTO = new PostResponseDTO();
        responseDTO.setPostId(1);
        responseDTO.setTitle("Test Blog Post");
    }

    @Test
    void testReadTimeCalculation() {
        // Arrange
        Post testPost = new Post();
        // Return DTO, not Entity!
        when(modelMapper.map(any(), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(postRepository.save(any())).thenReturn(testPost);

        StringBuilder longContent = new StringBuilder();
        for(int i=0; i<400; i++) longContent.append("word ");
        creationDTO.setContent(longContent.toString());

        // Act
        postService.createPost(creationDTO);

        // Assert
        verify(postRepository).save(argThat(post -> post.getReadTimeMin() >= 2));
    }
    
    @Test
    void testCreatePost_SlugGeneration() {
        // 1. Setup mocks
        Post savedPost = new Post();
        savedPost.setPostId(1);
        
        // Ensure the mock returns a DTO to avoid ClassCastException
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(modelMapper.map(any(), eq(PostResponseDTO.class))).thenReturn(responseDTO);

        // 2. Call the service
        PostResponseDTO result = postService.createPost(creationDTO);

        // 3. Assert
        assertNotNull(result);
        verify(postRepository, times(1)).save(any());
        // Verify slug logic specifically
        verify(postRepository).save(argThat(post -> post.getSlug().equals("test-blog-post")));
    }
}