package com.postservice.service;

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

	PostResponseDTO savePost(PostCreationDTO postDto, MultipartFile image);

	PostResponseDTO updateExistingPost(int postId, PostCreationDTO postDto, MultipartFile image);

	List<PostResponseDTO> getPostsByCategoryId(Integer catId);
}