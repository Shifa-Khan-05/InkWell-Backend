package com.postservice.service;

import com.postservice.client.AuthClient;

import com.postservice.client.TaxonomyClient;
import com.postservice.config.RabbitMQConfig;
import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.dto.UserResponseDTO;
import com.postservice.entity.Post;
import com.postservice.entity.PostLike;
import com.postservice.entity.SavedPost;
import com.postservice.repository.LikeRepository;
import com.postservice.repository.PostRepository;
import com.postservice.repository.SavedPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthClient authClient;

    @Mock
    private TaxonomyClient taxonomyClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SavedPostRepository savedPostRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post;
    private PostCreationDTO creationDTO;
    private PostResponseDTO responseDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setPostId(1);
        post.setTitle("Test Title");
        post.setContent("Test Content Test Content");
        post.setAuthorId(1);
        post.setCategoryId(1);
        post.setStatus("PUBLISHED");
        post.setSlug("test-title");
        post.setLikesCount(5);

        creationDTO = new PostCreationDTO();
        creationDTO.setTitle("Test Title");
        creationDTO.setContent("Test Content Test Content");
        creationDTO.setAuthorId(1);
        creationDTO.setCategoryId(1);
        creationDTO.setStatus("PUBLISHED");

        responseDTO = new PostResponseDTO();
        responseDTO.setPostId(1);
        responseDTO.setTitle("Test Title");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUserId(1);
        userResponseDTO.setFullName("Test User");
    }

    @Test
    void toggleSavePost_Saved_Unsaves() {
        when(savedPostRepository.existsByUserIdAndPostId(1, 1)).thenReturn(true);
        postService.toggleSavePost(1, 1);
        verify(savedPostRepository).deleteByUserIdAndPostId(1, 1);
    }

    @Test
    void toggleSavePost_Unsaved_Saves() {
        when(savedPostRepository.existsByUserIdAndPostId(1, 1)).thenReturn(false);
        postService.toggleSavePost(1, 1);
        verify(savedPostRepository).save(any(SavedPost.class));
    }

    @Test
    void getSavedPostsByUser_Success() {
        when(savedPostRepository.findByUserId(1)).thenReturn(List.of(new SavedPost(1, 1)));
        when(postRepository.findAllById(List.of(1))).thenReturn(List.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        List<PostResponseDTO> result = postService.getSavedPostsByUser(1);

        assertThat(result).hasSize(1);
        verify(authClient).getUserById(1);
    }

    @Test
    void isPostSavedByUser_ReturnsTrue() {
        when(savedPostRepository.existsByUserIdAndPostId(1, 1)).thenReturn(true);
        assertThat(postService.isPostSavedByUser(1, 1)).isTrue();
    }

    @Test
    void createPostWithImage_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(postRepository.existsBySlug(anyString())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.createPostWithImage(creationDTO, file);

        assertThat(result).isNotNull();
        verify(taxonomyClient).incrementPostCount(1);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.ROUTING_KEY), any(Object.class));
    }
    
    @Test
    void createPostWithImage_NullOriginalFilename() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", null, "image/jpeg", "test".getBytes());
        when(postRepository.existsBySlug(anyString())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.createPostWithImage(creationDTO, file);
        assertThat(result).isNotNull();
    }

    @Test
    void createPostWithImage_NoImage_DraftStatus() throws IOException {
        creationDTO.setStatus(null);
        post.setStatus("DRAFT");
        when(postRepository.existsBySlug(anyString())).thenReturn(true).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenThrow(new RuntimeException("Auth Service Down"));

        PostResponseDTO result = postService.createPostWithImage(creationDTO, null);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("InkWell Author");
        verify(taxonomyClient, never()).incrementPostCount(anyInt());
    }
    
    @Test
    void createPostWithImage_TaxonomySyncException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(postRepository.existsBySlug(anyString())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);
        doThrow(new RuntimeException("Taxonomy error")).when(taxonomyClient).incrementPostCount(1);

        PostResponseDTO result = postService.createPostWithImage(creationDTO, file);

        assertThat(result).isNotNull();
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.ROUTING_KEY), any(Object.class));
    }
    
    @Test
    void createPostWithImage_RabbitMQException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(postRepository.existsBySlug(anyString())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);
        doThrow(new RuntimeException("Rabbit error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        PostResponseDTO result = postService.createPostWithImage(creationDTO, file);

        assertThat(result).isNotNull();
    }
    
    @Test
    void createPostWithImage_EmptyContentNullExcerpt() throws IOException {
        creationDTO.setContent(null);
        creationDTO.setExcerpt(null);
        when(postRepository.existsBySlug(anyString())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.createPostWithImage(creationDTO, null);
        assertThat(result).isNotNull();
    }
    
    @Test
    void createPostWithImage_ShortContentNullExcerpt() throws IOException {
        creationDTO.setContent("Short content");
        creationDTO.setExcerpt(null);
        when(postRepository.existsBySlug(anyString())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.createPostWithImage(creationDTO, null);
        assertThat(result).isNotNull();
    }
    
    @Test
    void createPostWithImage_HasExcerpt() throws IOException {
        creationDTO.setExcerpt("My Excerpt");
        when(postRepository.existsBySlug(anyString())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponseDTO.class))).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.createPostWithImage(creationDTO, null);
        assertThat(result).isNotNull();
    }

    @Test
    void updatePost_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test2.jpg", "image/jpeg", "test".getBytes());
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.updatePost(1, creationDTO, file);

        assertThat(result).isNotNull();
    }
    
    @Test
    void updatePost_NoImage() throws IOException {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.updatePost(1, creationDTO, null);

        assertThat(result).isNotNull();
    }

    @Test
    void updatePost_NotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

        assertThatThrownBy(() -> postService.updatePost(1, creationDTO, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");
    }

    @Test
    void getPublishedPosts_Success() {
        when(postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED")).thenReturn(List.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        List<PostResponseDTO> result = postService.getPublishedPosts();

        assertThat(result).hasSize(1);
    }

    @Test
    void getPostBySlug_SuccessWithUser() {
        when(postRepository.findBySlugIgnoreCase("test-title")).thenReturn(Optional.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);
        when(likeRepository.existsByPostIdAndUserId(1, 2)).thenReturn(true);

        PostResponseDTO result = postService.getPostBySlug("test-title", 2);

        assertThat(result.isLikedByCurrentUser()).isTrue();
    }
    
    @Test
    void getPostBySlug_SuccessWithoutUser() {
        when(postRepository.findBySlugIgnoreCase("test-title")).thenReturn(Optional.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.getPostBySlug("test-title", 0);

        assertThat(result).isNotNull();
        verify(likeRepository, never()).existsByPostIdAndUserId(anyInt(), anyInt());
    }

    @Test
    void getPostBySlug_NotFound() {
        when(postRepository.findBySlugIgnoreCase("test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostBySlug("test", 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");
    }

    @Test
    void incrementLikes_Liked_Unlikes() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostIdAndUserId(1, 2)).thenReturn(true);

        postService.incrementLikes(1, 2);

        verify(likeRepository).deleteByPostIdAndUserId(1, 2);
        verify(postRepository).save(post);
        assertThat(post.getLikesCount()).isEqualTo(4);
    }

    @Test
    void incrementLikes_Unliked_LikesAndNotifies() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostIdAndUserId(1, 2)).thenReturn(false);

        postService.incrementLikes(1, 2);

        verify(likeRepository).save(any(PostLike.class));
        verify(postRepository).save(post);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.ROUTING_KEY), any(Object.class));
        assertThat(post.getLikesCount()).isEqualTo(6);
    }

    @Test
    void incrementLikes_Unliked_Likes_SameAuthor() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostIdAndUserId(1, 1)).thenReturn(false);

        postService.incrementLikes(1, 1);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void getPostsByCategoryId_Success() {
        when(postRepository.findByCategoryId(1)).thenReturn(List.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        List<PostResponseDTO> result = postService.getPostsByCategoryId(1);

        assertThat(result).hasSize(1);
    }
    
    @Test
    void getPostsByCategoryId_Empty() {
        when(postRepository.findByCategoryId(99)).thenReturn(List.of());
        List<PostResponseDTO> result = postService.getPostsByCategoryId(99);
        assertThat(result).isEmpty();
    }

    @Test
    void getPostById_Success() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        PostResponseDTO result = postService.getPostById(1);

        assertThat(result).isNotNull();
    }
    
    @Test
    void getPostById_NotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");
    }

    @Test
    void deletePost_Success() {
        postService.deletePost(1);
        verify(postRepository).deleteById(1);
    }

    @Test
    void getPostsByAuthor_Success() {
        when(postRepository.findByAuthorId(1)).thenReturn(List.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(responseDTO);
        when(authClient.getUserById(1)).thenReturn(userResponseDTO);

        List<PostResponseDTO> result = postService.getPostsByAuthor(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPostsByAuthor_Empty() {
        when(postRepository.findByAuthorId(99)).thenReturn(List.of());
        List<PostResponseDTO> result = postService.getPostsByAuthor(99);
        assertThat(result).isEmpty();
    }
}
