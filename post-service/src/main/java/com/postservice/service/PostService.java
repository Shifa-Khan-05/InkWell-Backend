package com.postservice.service;

import java.util.List;
import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;

public interface PostService {

    PostResponseDTO createPost(PostCreationDTO postDto);

    PostResponseDTO updatePost(int postId, PostCreationDTO postDto);

    PostResponseDTO getPostById(int id);

    PostResponseDTO getPostBySlug(String slug, int currentUserId);

    List<PostResponseDTO> getPostsByAuthor(int authorId);

    List<PostResponseDTO> getPublishedPosts();

    List<PostResponseDTO> getPostsByCategoryId(Integer catId);

    void deletePost(int postId);

    void incrementLikes(int postId, int userId);
}