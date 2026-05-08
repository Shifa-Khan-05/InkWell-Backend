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
import com.postservice.repository.PostRepository;
import com.postservice.repository.LikeRepository;
import com.postservice.repository.SavedPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

	@org.springframework.beans.factory.annotation.Value("${gateway.url:https://3.108.190.193.nip.io}")
    private String gatewayUrl;

	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.amazonaws.services.s3.AmazonS3 s3Client;

	@org.springframework.beans.factory.annotation.Value("${AWS_S3_BUCKET:inkwell-media-storage}")
	private String bucketName;

	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final ModelMapper modelMapper;
	private final AuthClient authClient;
	private final TaxonomyClient taxonomyClient;
	private final RabbitTemplate rabbitTemplate;
    private final SavedPostRepository savedPostRepository;

    @Override
    @Transactional
    public void toggleSavePost(int postId, int userId) {
        log.info("User {} toggling save on post ID: {}", userId, postId);
        if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
            savedPostRepository.deleteByUserIdAndPostId(userId, postId);
            log.debug("User {} unsaved post ID: {}", userId, postId);
        } else {
            savedPostRepository.save(new SavedPost(userId, postId));
            log.debug("User {} saved post ID: {}", userId, postId);
        }
    }

    @Override
    public List<PostResponseDTO> getSavedPostsByUser(int userId) {
        log.info("Fetching saved posts for user ID: {}", userId);
        List<Integer> postIds = savedPostRepository.findByUserId(userId)
                .stream()
                .map(SavedPost::getPostId)
                .toList();
        
        return postRepository.findAllById(postIds).stream()
                .map(this::enrichWithAuthor)
                .toList();
    }

    @Override
    public boolean isPostSavedByUser(int postId, int userId) {
        return savedPostRepository.existsByUserIdAndPostId(userId, postId);
    }

	@Override
	@Transactional
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public PostResponseDTO createPostWithImage(PostCreationDTO dto, MultipartFile image) throws IOException {
		log.info("Creating new post: {} for author: {}", dto.getTitle(), dto.getAuthorId());
		Post post = new Post();
		post.setTitle(dto.getTitle());
		post.setContent(dto.getContent());
		post.setAuthorId(dto.getAuthorId());
		post.setCategoryId(dto.getCategoryId());
		post.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");

		if (image != null && !image.isEmpty()) {
			log.debug("Saving featured image for post: {}", dto.getTitle());
			post.setFeaturedImageUrl(saveImageToDisk(image));
		}

		post.setSlug(generateUniqueSlug(dto.getTitle()));
		post.setExcerpt(generateExcerpt(dto));
		post.setReadTimeMin(calculateReadTime(dto.getContent()));
		post.setCreatedAt(LocalDateTime.now());
		post.setUpdatedAt(LocalDateTime.now());

		Post savedPost = postRepository.save(post);
		log.info("Post saved successfully with ID: {} and Slug: {}", savedPost.getPostId(), savedPost.getSlug());

		if ("PUBLISHED".equalsIgnoreCase(savedPost.getStatus())) {
			log.debug("Post published, triggering sync and notifications.");
			triggerTaxonomySync(savedPost);
			sendRabbitMessage(savedPost, "NEW_POST");
		}

		return enrichWithAuthor(savedPost);
	}

	@Override
	@Transactional
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public PostResponseDTO updatePost(int postId, PostCreationDTO dto, MultipartFile image) throws IOException {
		log.info("Updating post ID: {}", postId);
		Post post = postRepository.findById(postId).orElseThrow(() -> {
			log.error("Update failed: Post ID {} not found", postId);
			return new RuntimeException("Post not found");
		});

		post.setTitle(dto.getTitle());
		post.setContent(dto.getContent());
		post.setStatus(dto.getStatus());
		post.setExcerpt(generateExcerpt(dto));
		post.setReadTimeMin(calculateReadTime(dto.getContent()));
		post.setUpdatedAt(LocalDateTime.now());

		if (image != null && !image.isEmpty()) {
			log.debug("Updating featured image for post ID: {}", postId);
			post.setFeaturedImageUrl(saveImageToDisk(image));
		}

		Post updatedPost = postRepository.save(post);
		log.info("Post ID: {} updated successfully", postId);
		return enrichWithAuthor(updatedPost);
	}

	@Override
	@Cacheable(value = "publishedPosts")
	public List<PostResponseDTO> getPublishedPosts() {
		log.info("Fetching all published posts");
		return postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED").stream().map(this::enrichWithAuthor)
				.toList();
	}

	@Override
	public PostResponseDTO getPostBySlug(String slug, int currentUserId) {
		log.info("Fetching post by slug: {}", slug);
		Post post = postRepository.findBySlug(slug).orElseThrow(() -> {
			log.warn("Post lookup failed for slug: {}", slug);
			return new RuntimeException("Post not found");
		});
		PostResponseDTO dto = enrichWithAuthor(post);
		if (currentUserId > 0) {
			dto.setLikedByCurrentUser(likeRepository.existsByPostIdAndUserId(post.getPostId(), currentUserId));
		}
		return dto;
	}

	@Override
	@Transactional
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public void incrementLikes(int postId, int userId) {
		log.info("User {} toggling like on post ID: {}", userId, postId);
		Post post = postRepository.findById(postId).orElseThrow();
		if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
			likeRepository.deleteByPostIdAndUserId(postId, userId);
			post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
			log.debug("User {} unliked post ID: {}", userId, postId);
		} else {
			likeRepository.save(new PostLike(postId, userId));
			post.setLikesCount(post.getLikesCount() + 1);
			log.debug("User {} liked post ID: {}", userId, postId);
			if (userId != post.getAuthorId()) {
				sendRabbitMessage(post, "LIKE");
			}
		}
		postRepository.save(post);
	}

	@Override
	public List<PostResponseDTO> getPostsByCategoryId(Integer catId) {
		log.info("Fetching posts for category ID: {}", catId);
		return postRepository.findByCategoryId(catId).stream().map(this::enrichWithAuthor).toList();
	}

	@Override
	public PostResponseDTO getPostById(int id) {
		log.info("Fetching post by ID: {}", id);
		return postRepository.findById(id).map(this::enrichWithAuthor)
				.orElseThrow(() -> new RuntimeException("Post not found"));
	}

	@Override
	@Transactional
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public void deletePost(int postId) {
		log.info("Deleting post ID: {}", postId);
		postRepository.deleteById(postId);
	}

	@Override
	public List<PostResponseDTO> getPostsByAuthor(int authorId) {
		log.info("Fetching posts by author ID: {}", authorId);
		return postRepository.findByAuthorId(authorId).stream().map(this::enrichWithAuthor)
				.toList();
	}

	// --- Private Helpers ---

	private void sendRabbitMessage(Post post, String type) {
		log.debug("Sending RabbitMQ message [TYPE: {}] for post: {}", type, post.getPostId());
		Map<String, Object> message = new HashMap<>();
		message.put("postId", post.getPostId());
		message.put("title", post.getTitle());
		message.put("recipientId", post.getAuthorId());
		message.put("type", type);
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
		} catch (Exception e) {
			log.error("Failed to send RabbitMQ message: {}", e.getMessage());
		}
	}

	private String saveImageToDisk(MultipartFile image) throws IOException {
		String originalFilename = image.getOriginalFilename();
		if (originalFilename == null) {
			originalFilename = "default.jpg";
		}
		String fileName = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");

		if (s3Client != null) {
			try {
				com.amazonaws.services.s3.model.ObjectMetadata metadata = new com.amazonaws.services.s3.model.ObjectMetadata();
				metadata.setContentLength(image.getSize());
				metadata.setContentType(image.getContentType());
				s3Client.putObject(new com.amazonaws.services.s3.model.PutObjectRequest(bucketName, "post_uploads/" + fileName, image.getInputStream(), metadata)
					.withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead));
				return s3Client.getUrl(bucketName, "post_uploads/" + fileName).toString();
			} catch (Exception e) {
				log.warn("S3 upload failed, falling back to local storage: {}", e.getMessage());
			}
		}

		try {
			Path uploadPath = Paths.get("post_uploads");
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			Path filePath = uploadPath.resolve(fileName);
			log.info("Attempting to save file to absolute path: {}", filePath.toAbsolutePath());
			Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			return gatewayUrl + "/post_uploads/" + fileName;
		} catch (IOException e) {
			log.error("CRITICAL: Failed to save post image to disk. Check folder permissions (chmod 777 post_uploads). Error: {}", e.getMessage());
			throw new IOException("Server cannot write to storage: " + e.getMessage());
		}
	}

	private String generateUniqueSlug(String title) {
		String baseSlug = title.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
		String finalSlug = baseSlug;
		int attempts = 1;
		while (postRepository.existsBySlug(finalSlug)) {
			finalSlug = baseSlug + "-" + (System.currentTimeMillis() % 1000) + attempts++;
		}
		log.debug("Generated unique slug: {}", finalSlug);
		return finalSlug;
	}

	private PostResponseDTO enrichWithAuthor(Post post) {
		PostResponseDTO dto = modelMapper.map(post, PostResponseDTO.class);
		if (dto.getFeaturedImageUrl() != null && (dto.getFeaturedImageUrl().startsWith("http://localhost:8080/") || dto.getFeaturedImageUrl().startsWith("http://localhost:8081/"))) {
			dto.setFeaturedImageUrl(dto.getFeaturedImageUrl().replace("http://localhost:8080/", gatewayUrl + "/").replace("http://localhost:8081/", gatewayUrl + "/"));
		}
		
		try {
			log.debug("Fetching author info for ID: {}", post.getAuthorId());
			UserResponseDTO author = authClient.getUserById(post.getAuthorId());
			dto.setFullName(author.getFullName());
		} catch (Exception e) {
			log.warn("Author lookup failed for ID {}: {}. This usually means a connection issue to Auth-Service.", post.getAuthorId(), e.getMessage());
			dto.setFullName("InkWell Author");
		}
		return dto;
	}

	private int calculateReadTime(String content) {
		if (content == null)
			return 1;
		return Math.max(1, content.split("\\s+").length / 200);
	}

	private String generateExcerpt(PostCreationDTO dto) {
		if (dto.getExcerpt() != null && !dto.getExcerpt().isEmpty())
			return dto.getExcerpt();
		if (dto.getContent() == null)
			return "";
		return dto.getContent().length() > 150 ? dto.getContent().substring(0, 150) + "..." : dto.getContent();
	}

	private void triggerTaxonomySync(Post post) {
		if (post.getCategoryId() != null) {
			log.debug("Syncing taxonomy post count for category ID: {}", post.getCategoryId());
			try {
				taxonomyClient.incrementPostCount(post.getCategoryId());
			} catch (Exception e) {
				log.error("Taxonomy sync failed: {}", e.getMessage());
			}
		}
	}
}
