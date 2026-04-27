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
import com.postservice.client.TaxonomyClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ModelMapper modelMapper;
    
    @Mock
    private AuthClient authClient;

    @Mock
    private TaxonomyClient taxonomyClient;

    @Mock
    private RabbitTemplate rabbitTemplate; // ✅ Added to handle messaging logic in tests

    @InjectMocks
    private PostServiceImpl postService;

    private PostCreationDTO creationDTO;
    private PostResponseDTO responseDTO;
    private MultipartFile mockImage; // ✅ Added for multipart testing

    @BeforeEach
    void setUp() {
        creationDTO = new PostCreationDTO();
        creationDTO.setTitle("Test Blog Post");
        creationDTO.setContent("This is a test content.");
        creationDTO.setAuthorId(1);
        creationDTO.setStatus("DRAFT");

        responseDTO = new PostResponseDTO();
        responseDTO.setPostId(1);
        responseDTO.setTitle("Test Blog Post");

        mockImage = mock(MultipartFile.class);
    }

    @Test
    void testReadTimeCalculation() throws IOException {
        // Arrange
        Post testPost = new Post();
        when(modelMapper.map(any(), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(postRepository.save(any())).thenReturn(testPost);

        StringBuilder longContent = new StringBuilder();
        for(int i=0; i<400; i++) longContent.append("word ");
        creationDTO.setContent(longContent.toString());

        // Act - Changed to createPostWithImage
        postService.createPostWithImage(creationDTO, null);

        // Assert
        verify(postRepository).save(argThat(post -> post.getReadTimeMin() >= 2));
    }
    
    @Test
    void testCreatePost_SlugGeneration() throws IOException {
        // Arrange
        Post savedPost = new Post();
        savedPost.setPostId(1);
        
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(modelMapper.map(any(), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(postRepository.existsBySlug(anyString())).thenReturn(false); // ✅ Required for slug logic

        // Act - Changed to createPostWithImage
        PostResponseDTO result = postService.createPostWithImage(creationDTO, null);

        // Assert
        assertNotNull(result);
        verify(postRepository, times(1)).save(any());
        verify(postRepository).save(argThat(post -> post.getSlug().contains("test-blog-post")));
    }
}