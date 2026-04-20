package com.postservice.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.postservice.client.AuthClient;
import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.dto.UserResponseDTO;
import com.postservice.entity.Post;
import com.postservice.entity.PostLike;
import com.postservice.repository.PostRepository;
import com.postservice.repository.LikeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final LikeRepository likeRepository; // ✅ Added to track unique likes
	private final ModelMapper modelMapper;
	private final AuthClient authClient;

	@Override
	public PostResponseDTO createPost(PostCreationDTO postDto) {
		Post post = new Post();
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setExcerpt(postDto.getExcerpt());
		post.setAuthorId(postDto.getAuthorId());
		post.setFeaturedImageUrl(postDto.getFeaturedImageUrl());

		String slug = postDto.getTitle().toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
		post.setSlug(slug);

		int wordCount = postDto.getContent().split("\\s+").length;
		post.setReadTimeMin(Math.max(1, wordCount / 200));

		post.setStatus(postDto.getStatus() != null ? postDto.getStatus() : "DRAFT");

		Post savedPost = postRepository.save(post);
		return modelMapper.map(savedPost, PostResponseDTO.class);
	}

	@Override
	public List<PostResponseDTO> getPostsByAuthor(int authorId) {
		return postRepository.findByAuthorId(authorId).stream()
				.map(post -> modelMapper.map(post, PostResponseDTO.class)).collect(Collectors.toList());
	}

	@Override
	public PostResponseDTO getPostById(int id) {
		Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
		return enrichWithAuthor(post);
	}

	@Override
	public PostResponseDTO updatePost(int postId, PostCreationDTO postDto) {
		Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setExcerpt(postDto.getExcerpt());
		post.setStatus(postDto.getStatus());
		post.setSlug(postDto.getTitle().toLowerCase().trim().replaceAll("[^a-z0-9]+", "-"));

		Post updated = postRepository.save(post);
		return modelMapper.map(updated, PostResponseDTO.class);
	}

	@Override
	public void deletePost(int postId) {
		postRepository.deleteById(postId);
	}

	@Override
	public List<PostResponseDTO> getPublishedPosts() {
		return postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED").stream().map(this::enrichWithAuthor)
				.collect(Collectors.toList());
	}

	@Override
	public PostResponseDTO getPostBySlug(String slug, int currentUserId) {
		// 1. Fetch the post entity
		Post post = postRepository.findBySlug(slug).orElseThrow(() -> new RuntimeException("Post not found"));

		// 2. Map to DTO and enrich with Author name
		PostResponseDTO dto = enrichWithAuthor(post);

		// 3. FORCE update the likesCount from the entity to the DTO
		dto.setLikesCount(post.getLikesCount());

		// 4. ✅ THE FIX: Check if a permanent record exists in the likes table
		if (currentUserId > 0) {
			boolean hasLiked = likeRepository.existsByPostIdAndUserId(post.getPostId(), currentUserId);
			dto.setLikedByCurrentUser(hasLiked); // This tells React the heart should be red
		} else {
			dto.setLikedByCurrentUser(false);
		}

		return dto;
	}

	@Override
	@Transactional // Ensures atomic updates for both tables
	public void incrementLikes(int postId, int userId) {
	    // 1. Fetch the post
	    Post post = postRepository.findById(postId)
	            .orElseThrow(() -> new RuntimeException("Post not found"));

	    // 2. Check if the user has already liked this post
	    if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
	        // ❌ UNLIKE LOGIC: Remove the record and decrease count
	        likeRepository.deleteByPostIdAndUserId(postId, userId);
	        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
	    } else {
	        // ❤️ LIKE LOGIC: Add the record and increase count
	        likeRepository.save(new PostLike(postId, userId));
	        post.setLikesCount(post.getLikesCount() + 1);
	    }

	    // 3. Save the updated post count
	    postRepository.save(post);
	}

	// ✅ Reusable Helper to fetch Author Name from Auth-Service via Feign
	private PostResponseDTO enrichWithAuthor(Post post) {
		PostResponseDTO dto = modelMapper.map(post, PostResponseDTO.class);
		// Explicitly map likesCount as it's the most critical field for refresh
		dto.setLikesCount(post.getLikesCount());

		try {
			UserResponseDTO author = authClient.getUserById(post.getAuthorId());
			dto.setFullName(author.getFullName());
		} catch (Exception e) {
			dto.setFullName("InkWell Author");
		}
		return dto;
	}
}