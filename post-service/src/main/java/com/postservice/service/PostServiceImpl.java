package com.postservice.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final ModelMapper modelMapper;
	private final AuthClient authClient;

	@Override
	public PostResponseDTO createPost(PostCreationDTO postDto) {
		Post post = modelMapper.map(postDto, Post.class);
		post.setSlug(generateSlug(postDto.getTitle()));
		post.setReadTimeMin(calculateReadTime(postDto.getContent()));
		post.setStatus(postDto.getStatus() != null ? postDto.getStatus() : "DRAFT");
		post.setLikesCount(0);

		Post savedPost = postRepository.save(post);
		return modelMapper.map(savedPost, PostResponseDTO.class);
	}

	@Override
	@Transactional
	public PostResponseDTO createPostWithImage(PostCreationDTO postDto, MultipartFile image) throws IOException {
		if (image != null && !image.isEmpty()) {
			postDto.setFeaturedImageUrl(saveImage(image));
		}
		return createPost(postDto);
	}

	@Override
	@Transactional
	public PostResponseDTO updatePostWithImage(int postId, PostCreationDTO postDto, MultipartFile image)
			throws IOException {
		Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

		if (image != null && !image.isEmpty()) {
			post.setFeaturedImageUrl(saveImage(image));
		}

		post.setTitle(postDto.getTitle());
		post.setContent(postDto.getContent());
		post.setExcerpt(postDto.getExcerpt());
		post.setStatus(postDto.getStatus());
		post.setSlug(generateSlug(postDto.getTitle()));
		post.setReadTimeMin(calculateReadTime(postDto.getContent()));

		Post updated = postRepository.save(post);
		return modelMapper.map(updated, PostResponseDTO.class);
	}

	private String saveImage(MultipartFile image) throws IOException {
		String uploadDir = "post_uploads/";
		java.io.File directory = new java.io.File(uploadDir);
		if (!directory.exists())
			directory.mkdirs();

		String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
		java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + fileName);
		java.nio.file.Files.write(path, image.getBytes());

		// ✅ Use only ONE slash here
		return "http://localhost:8080/post_uploads/" + fileName;
	}

	private int calculateReadTime(String content) {
		if (content == null)
			return 1;
		int wordCount = content.split("\\s+").length;
		return Math.max(1, wordCount / 200);
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
		post.setStatus(postDto.getStatus());
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
		Post post = postRepository.findBySlug(slug).orElseThrow(() -> new RuntimeException("Post not found"));
		PostResponseDTO dto = enrichWithAuthor(post);
		dto.setLikesCount(post.getLikesCount());

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

	private PostResponseDTO enrichWithAuthor(Post post) {
		PostResponseDTO dto = modelMapper.map(post, PostResponseDTO.class);
		try {
			// Fetch user details from Auth-Service via Feign Client
			UserResponseDTO author = authClient.getUserById(post.getAuthorId());

			// ✅ Set the actual name from the Auth Service
			dto.setAuthorName(author.getFullName());
		} catch (Exception e) {
			dto.setAuthorName("InkWell User"); // Fallback
		}
		return dto;
	}

	private String generateSlug(String title) {
		String baseSlug = title.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
		// Append a small random string or timestamp to guarantee uniqueness
		return baseSlug + "-" + System.currentTimeMillis() % 10000;
	}
}