package com.postservice.controller;

import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostResource {

    private final PostService postService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "excerpt", required = false) String excerpt,
            @RequestParam("authorId") int authorId,
            @RequestParam(value = "status", defaultValue = "DRAFT") String status,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        PostCreationDTO postDto = new PostCreationDTO();
        postDto.setTitle(title);
        postDto.setContent(content);
        postDto.setExcerpt(excerpt);
        postDto.setAuthorId(authorId);
        postDto.setStatus(status);

        return new ResponseEntity<>(postService.createPostWithImage(postDto, image), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable int postId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "excerpt", required = false) String excerpt,
            @RequestParam("status") String status,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        PostCreationDTO postDto = new PostCreationDTO();
        postDto.setTitle(title);
        postDto.setContent(content);
        postDto.setExcerpt(excerpt);
        postDto.setStatus(status);

        return ResponseEntity.ok(postService.updatePostWithImage(postId, postDto, image));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<PostResponseDTO>> getByAuthor(@PathVariable int authorId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
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

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getById(@PathVariable int id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable int postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}