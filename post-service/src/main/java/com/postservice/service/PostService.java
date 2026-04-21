package com.postservice.service;

import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;

public interface PostService {
	PostResponseDTO createPost(PostCreationDTO postDto);

	PostResponseDTO getPostById(int id);

	List<PostResponseDTO> getPostsByAuthor(int authorId);

	void deletePost(int postId);

	PostResponseDTO updatePost(int postId, PostCreationDTO postDto);

	List<PostResponseDTO> getPublishedPosts();

	PostResponseDTO getPostBySlug(String slug, int currentUserId);

	void incrementLikes(int postId, int userId);

	// ✅ Main method for handling text + image
	PostResponseDTO createPostWithImage(PostCreationDTO postDto, MultipartFile image) throws IOException;

	// ✅ Method for handling updates with optional new images
	PostResponseDTO updatePostWithImage(int postId, PostCreationDTO postDto, MultipartFile image) throws IOException;
}