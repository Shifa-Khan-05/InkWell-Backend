package com.postservice.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.postservice.client.AuthClient;
import com.postservice.client.TaxonomyClient;
import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.dto.UserResponseDTO;
import com.postservice.entity.Post;
import com.postservice.entity.PostLike;
import com.postservice.repository.PostRepository;
import com.postservice.repository.LikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ModelMapper modelMapper;
    private final AuthClient authClient;
    private final TaxonomyClient taxonomyClient;

    @Override
    @Transactional
    public PostResponseDTO savePost(PostCreationDTO postDto, MultipartFile image) {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setExcerpt(postDto.getExcerpt() != null ? postDto.getExcerpt() : "");
        post.setAuthorId(postDto.getAuthorId());
        post.setStatus(postDto.getStatus() != null ? postDto.getStatus() : "DRAFT");
        
        // ✅ CRITICAL: Map the Category ID from DTO to Entity
        post.setCategoryId(postDto.getCategoryId());

        // Generate SEO Slug
        String slug = postDto.getTitle().toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
        post.setSlug(slug);

        // Calculate Read Time
        int wordCount = postDto.getContent().split("\\s+").length;
        post.setReadTimeMin(Math.max(1, wordCount / 200));

        // Handle Image
        if (image != null && !image.isEmpty()) {
            post.setFeaturedImageUrl(handleImageUpload(image));
        }

        Post savedPost = postRepository.save(post);

        // ✅ SYNC: Trigger Taxonomy Service count update if published
        if ("PUBLISHED".equalsIgnoreCase(savedPost.getStatus()) && savedPost.getCategoryId() != null) {
            try {
                taxonomyClient.incrementPostCount(savedPost.getCategoryId());
            } catch (Exception e) {
                System.err.println("Taxonomy increment failed: " + e.getMessage());
            }
        }

        return enrichWithAuthor(savedPost);
    }

    @Override
    @Transactional
    public PostResponseDTO updateExistingPost(int postId, PostCreationDTO postDto, MultipartFile image) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        boolean previouslyPublished = "PUBLISHED".equalsIgnoreCase(post.getStatus());

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setExcerpt(postDto.getExcerpt());
        post.setStatus(postDto.getStatus());
        
        // ✅ Update Category ID
        post.setCategoryId(postDto.getCategoryId());
        
        // Update Slug
        post.setSlug(postDto.getTitle().toLowerCase().trim().replaceAll("[^a-z0-9]+", "-"));

        if (image != null && !image.isEmpty()) {
            post.setFeaturedImageUrl(handleImageUpload(image));
        }

        Post updated = postRepository.save(post);

        // ✅ SYNC: Only increment if it's newly published
        if (!previouslyPublished && "PUBLISHED".equalsIgnoreCase(updated.getStatus()) && updated.getCategoryId() != null) {
            try {
                taxonomyClient.incrementPostCount(updated.getCategoryId());
            } catch (Exception e) {
                System.err.println("Taxonomy increment failed during update.");
            }
        }

        return enrichWithAuthor(updated);
    }

    // ✅ HELPER: Reusable Image Upload Logic
    private String handleImageUpload(MultipartFile image) {
        try {
            String uploadDir = "uploads/posts/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = "post_" + System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            
            return "http://localhost:8082/uploads/posts/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("File storage failed");
        }
    }

    @Override
    public List<PostResponseDTO> getPostsByAuthor(int authorId) {
        return postRepository.findByAuthorId(authorId).stream()
                .map(this::enrichWithAuthor).collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostById(int id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        return enrichWithAuthor(post);
    }

    @Override
    public void deletePost(int postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public List<PostResponseDTO> getPublishedPosts() {
        return postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED").stream()
                .map(this::enrichWithAuthor).collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostBySlug(String slug, int currentUserId) {
        Post post = postRepository.findBySlug(slug).orElseThrow(() -> new RuntimeException("Post not found"));
        PostResponseDTO dto = enrichWithAuthor(post);
        dto.setLikesCount(post.getLikesCount());

        if (currentUserId > 0) {
            boolean hasLiked = likeRepository.existsByPostIdAndUserId(post.getPostId(), currentUserId);
            dto.setLikedByCurrentUser(hasLiked);
        }
        return dto;
    }

    @Override
    @Transactional
    public void incrementLikes(int postId, int userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            likeRepository.deleteByPostIdAndUserId(postId, userId);
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        } else {
            likeRepository.save(new PostLike(postId, userId));
            post.setLikesCount(post.getLikesCount() + 1);
        }
        postRepository.save(post);
    }

    private PostResponseDTO enrichWithAuthor(Post post) {
        PostResponseDTO dto = modelMapper.map(post, PostResponseDTO.class);
        dto.setLikesCount(post.getLikesCount());
        try {
            UserResponseDTO author = authClient.getUserById(post.getAuthorId());
            dto.setFullName(author.getFullName());
        } catch (Exception e) {
            dto.setFullName("InkWell Author");
        }
        return dto;
    }

    @Override
    public PostResponseDTO createPost(PostCreationDTO postDto) {
        return savePost(postDto, null);
    }

    @Override
    public PostResponseDTO updatePost(int postId, PostCreationDTO postDto) {
        return updateExistingPost(postId, postDto, null);
    }
    
    @Override
    public List<PostResponseDTO> getPostsByCategoryId(Integer catId) {
        // Assuming you add 'findByCategoryId' to your PostRepository
        return postRepository.findByCategoryId(catId).stream()
                .map(this::enrichWithAuthor)
                .collect(Collectors.toList());
    }
}