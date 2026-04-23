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

    @PostMapping("/create")
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody PostCreationDTO postDto) {
        return ResponseEntity.ok(postService.createPost(postDto));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable int postId, 
            @RequestBody PostCreationDTO postDto) {
        return ResponseEntity.ok(postService.updatePost(postId, postDto));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<PostResponseDTO>> getByAuthor(@PathVariable int authorId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getById(@PathVariable int id) {
        return ResponseEntity.ok(postService.getPostById(id));
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

    @GetMapping("/category/{catId}")
    public ResponseEntity<List<PostResponseDTO>> getByCategoryId(@PathVariable Integer catId) {
        return ResponseEntity.ok(postService.getPostsByCategoryId(catId));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> incrementLikes(
            @PathVariable int postId, 
            @RequestParam(name = "userId") int userId) {
        postService.incrementLikes(postId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable int postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}