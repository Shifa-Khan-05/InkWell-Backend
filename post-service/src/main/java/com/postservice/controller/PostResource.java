package com.postservice.controller;

import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType; // ✅ CORRECT IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PostResource {

    private final PostService postService;

    /**
     * Requirement 2.3: Create a new post with Image Upload.
     * Use @ModelAttribute to bind form-data (Text + File).
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> createPost(
            @ModelAttribute PostCreationDTO postDto, 
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(postService.savePost(postDto, image));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<PostResponseDTO>> getByAuthor(@PathVariable int authorId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getById(@PathVariable int id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable int postId, 
            @ModelAttribute PostCreationDTO postDto, 
            @RequestParam(value = "image", required = false) MultipartFile image) {
        // ✅ Use the savePost logic but for an existing ID
        return ResponseEntity.ok(postService.updateExistingPost(postId, postDto, image));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable int postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/published")
    public ResponseEntity<List<PostResponseDTO>> getPublishedPosts() {
        return ResponseEntity.ok(postService.getPublishedPosts());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponseDTO> getPostBySlug(
            @PathVariable String slug,
            @RequestParam(name = "userId", required = false, defaultValue = "0") int userId) {
        return ResponseEntity.ok(postService.getPostBySlug(slug, userId));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> incrementLikes(
            @PathVariable int postId, 
            @RequestParam(name = "userId") int userId) {
        postService.incrementLikes(postId, userId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/category/{catId}")
    public ResponseEntity<List<PostResponseDTO>> getByCategoryId(@PathVariable Integer catId) {
        return ResponseEntity.ok(postService.getPostsByCategoryId(catId));
    }
}