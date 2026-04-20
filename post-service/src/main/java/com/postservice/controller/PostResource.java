package com.postservice.controller;

import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PostResource {

    private final PostService postService;

    /**
     * Create a new post (Draft or Published)
     */
    @PostMapping("/create")
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody PostCreationDTO postDto) {
        return ResponseEntity.ok(postService.createPost(postDto));
    }

    /**
     * Get all posts by a specific author (used in Author Studio)
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<PostResponseDTO>> getByAuthor(@PathVariable int authorId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
    }

    /**
     * Get post by numeric ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getById(@PathVariable int id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    /**
     * Requirement 2.4: Update existing post content or status
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(@PathVariable int postId, @RequestBody PostCreationDTO postDto) {
        return ResponseEntity.ok(postService.updatePost(postId, postDto));
    }

    /**
     * Requirement 2.4: Delete a post permanently
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable int postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Fetch only posts with status 'PUBLISHED' for the Browse/Reader feed
     */
    @GetMapping("/published")
    public ResponseEntity<List<PostResponseDTO>> getPublishedPosts() {
        return ResponseEntity.ok(postService.getPublishedPosts());
    }

    /**
     * Requirement 2.1: Fetch full post content by SEO-friendly slug.
     * Fixed the 'RequestPSaram' typo here.
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponseDTO> getPostBySlug(
            @PathVariable String slug,
            @RequestParam(name = "userId", required = false, defaultValue = "0") int userId) {
        return ResponseEntity.ok(postService.getPostBySlug(slug, userId));
    }

    /**
     * Requirement 2.2: Readers can like posts.
     * Fixed the 'RequestPSaram' typo here.
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> incrementLikes(
            @PathVariable int postId, 
            @RequestParam(name = "userId") int userId) {
        postService.incrementLikes(postId, userId);
        return ResponseEntity.ok().build();
    }
}