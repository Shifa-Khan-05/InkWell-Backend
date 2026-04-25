package com.postservice.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.postservice.client.AuthClient;
import com.postservice.client.NotificationClient;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ModelMapper modelMapper;
    private final AuthClient authClient;
    private final TaxonomyClient taxonomyClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public PostResponseDTO createPost(PostCreationDTO dto) {
        Post post = new Post();
        
        // 1. Basic Mapping
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setAuthorId(dto.getAuthorId());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");
        post.setCategoryId(dto.getCategoryId());
        post.setFeaturedImageUrl(dto.getFeaturedImageUrl()); // Received from Frontend/Media-Service

        // 2. Automated Meta-data
        post.setExcerpt(generateExcerpt(dto));
        post.setReadTimeMin(calculateReadTime(dto.getContent()));
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // 3. Generate Unique Slug (Fixes 500 Error)
        post.setSlug(generateUniqueSlug(dto.getTitle()));

        Post savedPost = postRepository.save(post);

        // 4. Sync with Taxonomy Service
        triggerTaxonomySync(savedPost);

        return enrichWithAuthor(savedPost);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePost(int postId, PostCreationDTO dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Manuscript not found"));

        boolean statusChangedToPublished = !"PUBLISHED".equalsIgnoreCase(post.getStatus()) 
                                          && "PUBLISHED".equalsIgnoreCase(dto.getStatus());

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setExcerpt(dto.getExcerpt());
        post.setStatus(dto.getStatus());
        post.setCategoryId(dto.getCategoryId());
        post.setFeaturedImageUrl(dto.getFeaturedImageUrl());
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        if (statusChangedToPublished) {
            triggerTaxonomySync(updatedPost);
        }

        return enrichWithAuthor(updatedPost);
    }

    // ✅ HELPER: Generate unique slug by appending timestamp if duplicate found
    private String generateUniqueSlug(String title) {
        String baseSlug = title.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
        String finalSlug = baseSlug;
        int attempts = 1;
        
        while (postRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + (System.currentTimeMillis() % 1000) + attempts;
            attempts++;
        }
        return finalSlug;
    }

    // ✅ HELPER: Logic to handle Author Details via Feign
    private PostResponseDTO enrichWithAuthor(Post post) {
        PostResponseDTO dto = modelMapper.map(post, PostResponseDTO.class);
        // Ensure image URL is explicitly set if mapper misses it
        dto.setFeaturedImageUrl(post.getFeaturedImageUrl());
        dto.setLikesCount(post.getLikesCount());
        
        try {
            UserResponseDTO author = authClient.getUserById(post.getAuthorId());
            dto.setFullName(author.getFullName());
        } catch (Exception e) {
            dto.setFullName("InkWell Author");
        }
        return dto;
    }

    private String generateExcerpt(PostCreationDTO dto) {
        if (dto.getExcerpt() != null && !dto.getExcerpt().isEmpty()) return dto.getExcerpt();
        return dto.getContent().length() > 150 ? dto.getContent().substring(0, 150) + "..." : dto.getContent();
    }

    private int calculateReadTime(String content) {
        if (content == null) return 1;
        int wordCount = content.split("\\s+").length;
        return Math.max(1, wordCount / 200);
    }

    private void triggerTaxonomySync(Post post) {
        if ("PUBLISHED".equalsIgnoreCase(post.getStatus()) && post.getCategoryId() != null) {
            try {
                taxonomyClient.incrementPostCount(post.getCategoryId());
            } catch (Exception ignored) {}
        }
    }

    // ✅ Standard List Fetchers
    @Override
    public List<PostResponseDTO> getPublishedPosts() {
        return postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED").stream()
                .map(this::enrichWithAuthor).collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostsByAuthor(int authorId) {
        return postRepository.findByAuthorId(authorId).stream()
                .map(this::enrichWithAuthor).collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostsByCategoryId(Integer catId) {
        return postRepository.findByCategoryId(catId).stream()
                .map(this::enrichWithAuthor).collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostById(int id) {
        return postRepository.findById(id).map(this::enrichWithAuthor)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public PostResponseDTO getPostBySlug(String slug, int currentUserId) {
        Post post = postRepository.findBySlug(slug).orElseThrow(() -> new RuntimeException("Post not found"));
        PostResponseDTO dto = enrichWithAuthor(post);
        if (currentUserId > 0) {
            dto.setLikedByCurrentUser(likeRepository.existsByPostIdAndUserId(post.getPostId(), currentUserId));
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

    @Override
    public void deletePost(int postId) {
        postRepository.deleteById(postId);
    }
    
    
    @Transactional
    public void likePost(Integer postId, Integer userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        
        // Logic to save the like in DB...

        // ✨ Send Notification to Author
        if (!userId.equals(post.getAuthorId())) { // Don't notify if I like my own post
            Map<String, Object> note = new HashMap<>();
            note.put("recipientId", post.getAuthorId());
            note.put("actorId", userId);
            note.put("type", "LIKE");
            note.put("message", "Someone appreciated your manuscript: " + post.getTitle());
            note.put("relatedId", postId);

            notificationClient.sendNotification(note);
        }
    }

}