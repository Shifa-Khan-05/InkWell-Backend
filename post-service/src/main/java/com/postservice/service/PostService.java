package com.postservice.service;

import java.util.List;

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
}