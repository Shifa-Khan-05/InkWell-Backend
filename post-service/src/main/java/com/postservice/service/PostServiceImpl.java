package com.postservice.service;

import com.postservice.client.AuthClient;
import com.postservice.client.TaxonomyClient;
import com.postservice.config.RabbitMQConfig;
import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.dto.UserResponseDTO;
import com.postservice.entity.Post;
import com.postservice.entity.PostLike;
import com.postservice.repository.PostRepository;
import com.postservice.repository.LikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final ModelMapper modelMapper;
	private final AuthClient authClient;
	private final TaxonomyClient taxonomyClient;
	private final RabbitTemplate rabbitTemplate;

	@Override
	@Transactional
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public PostResponseDTO createPostWithImage(PostCreationDTO dto, MultipartFile image) throws IOException {
		Post post = new Post();
		post.setTitle(dto.getTitle());
		post.setContent(dto.getContent());
		post.setAuthorId(dto.getAuthorId());
		post.setCategoryId(dto.getCategoryId());
		post.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");

		if (image != null && !image.isEmpty()) {
			post.setFeaturedImageUrl(saveImageToDisk(image));
		}

		post.setSlug(generateUniqueSlug(dto.getTitle()));
		post.setExcerpt(generateExcerpt(dto));
		post.setReadTimeMin(calculateReadTime(dto.getContent()));
		post.setCreatedAt(LocalDateTime.now());
		post.setUpdatedAt(LocalDateTime.now());

		Post savedPost = postRepository.save(post);

		if ("PUBLISHED".equalsIgnoreCase(savedPost.getStatus())) {
			triggerTaxonomySync(savedPost);
			sendRabbitMessage(savedPost, "NEW_POST");
		}

		return enrichWithAuthor(savedPost);
	}

	@Override
	@Transactional
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public PostResponseDTO updatePost(int postId, PostCreationDTO dto, MultipartFile image) throws IOException {
		Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

		post.setTitle(dto.getTitle());
		post.setContent(dto.getContent());
		post.setStatus(dto.getStatus());
		post.setExcerpt(generateExcerpt(dto));
		post.setReadTimeMin(calculateReadTime(dto.getContent()));
		post.setUpdatedAt(LocalDateTime.now());

		if (image != null && !image.isEmpty()) {
			post.setFeaturedImageUrl(saveImageToDisk(image));
		}

		Post updatedPost = postRepository.save(post);
		return enrichWithAuthor(updatedPost);
	}

	@Override
	@Cacheable(value = "publishedPosts")
	public List<PostResponseDTO> getPublishedPosts() {
		return postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED").stream().map(this::enrichWithAuthor)
				.collect(Collectors.toList());
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
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public void incrementLikes(int postId, int userId) {
		Post post = postRepository.findById(postId).orElseThrow();
		if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
			likeRepository.deleteByPostIdAndUserId(postId, userId);
			post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
		} else {
			likeRepository.save(new PostLike(postId, userId));
			post.setLikesCount(post.getLikesCount() + 1);
			if (userId != post.getAuthorId()) {
				sendRabbitMessage(post, "LIKE");
			}
		}
		postRepository.save(post);
	}

	@Override
	public List<PostResponseDTO> getPostsByCategoryId(Integer catId) {
		return postRepository.findByCategoryId(catId).stream().map(this::enrichWithAuthor).collect(Collectors.toList());
	}

	@Override
	public PostResponseDTO getPostById(int id) {
		return postRepository.findById(id).map(this::enrichWithAuthor)
				.orElseThrow(() -> new RuntimeException("Post not found"));
	}

	@Override
	@Transactional
	@CacheEvict(value = "publishedPosts", allEntries = true)
	public void deletePost(int postId) {
		postRepository.deleteById(postId);
	}

	@Override
	public List<PostResponseDTO> getPostsByAuthor(int authorId) {
		return postRepository.findByAuthorId(authorId).stream().map(this::enrichWithAuthor)
				.collect(Collectors.toList());
	}

	// --- Private Helpers ---

	private void sendRabbitMessage(Post post, String type) {
		Map<String, Object> message = new HashMap<>();
		message.put("postId", post.getPostId());
		message.put("title", post.getTitle());
		message.put("recipientId", post.getAuthorId());
		message.put("type", type);
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
	}

	private String saveImageToDisk(MultipartFile image) throws IOException {
		String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
		Path uploadPath = Paths.get("uploads");

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Path filePath = uploadPath.resolve(fileName);
		Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		return "http://localhost:8080/post_uploads/" + fileName;
	}

	private String generateUniqueSlug(String title) {
		String baseSlug = title.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
		String finalSlug = baseSlug;
		int attempts = 1;
		while (postRepository.existsBySlug(finalSlug)) {
			finalSlug = baseSlug + "-" + (System.currentTimeMillis() % 1000) + attempts++;
		}
		return finalSlug;
	}

	private PostResponseDTO enrichWithAuthor(Post post) {
		PostResponseDTO dto = modelMapper.map(post, PostResponseDTO.class);
		try {
			UserResponseDTO author = authClient.getUserById(post.getAuthorId());
			dto.setFullName(author.getFullName());
		} catch (Exception e) {
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
			try {
				taxonomyClient.incrementPostCount(post.getCategoryId());
			} catch (Exception ignored) {
			}
		}
	}
}