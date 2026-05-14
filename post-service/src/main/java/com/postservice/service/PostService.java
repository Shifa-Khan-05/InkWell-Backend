package com.postservice.service;

import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;

public interface PostService {
	//  Post Management
	PostResponseDTO createPostWithImage(PostCreationDTO postDto, MultipartFile image) throws IOException;

	PostResponseDTO updatePost(int postId, PostCreationDTO postDto, MultipartFile image) throws IOException;

	void deletePost(int postId);

	// Fetching Data
	PostResponseDTO getPostById(int id);

	PostResponseDTO getPostBySlug(String slug, int currentUserId);

	List<PostResponseDTO> getPostsByAuthor(int authorId);

	List<PostResponseDTO> getPublishedPosts();

	List<PostResponseDTO> getPostsByCategoryId(Integer catId);

	// Interaction
	void incrementLikes(int postId, int userId);

    // 🔖 Saved Posts (Pro Feature)
    void toggleSavePost(int postId, int userId);
    List<PostResponseDTO> getSavedPostsByUser(int userId);
    boolean isPostSavedByUser(int postId, int userId);
}