package com.postservice.controller;

import com.postservice.dto.PostCreationDTO;
import com.postservice.dto.PostResponseDTO;
import com.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostResource {

	private final PostService postService;

	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<PostResponseDTO> createPost(@ModelAttribute PostCreationDTO dto,
			@RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
		log.info("DIAGNOSTIC: createPost called with DTO: {}", dto);
		if (image != null) {
			log.info("DIAGNOSTIC: Image received: {}, size: {}", image.getOriginalFilename(), image.getSize());
		} else {
			log.warn("DIAGNOSTIC: No image received in multipart request");
		}
		
		try {
			PostResponseDTO response = postService.createPostWithImage(dto, image);
			log.info("DIAGNOSTIC: Post created successfully with ID: {}", response.getPostId());
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("DIAGNOSTIC: CRITICAL FAILURE in createPost: {}", e.getMessage(), e);
			throw e;
		}
	}

	@PutMapping(value = "/update/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<PostResponseDTO> updatePost(@PathVariable int postId, @ModelAttribute PostCreationDTO postDto,
			@RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
		return ResponseEntity.ok(postService.updatePost(postId, postDto, image));
	}

	@GetMapping("/published")
	public ResponseEntity<List<PostResponseDTO>> getPublishedPosts() {
		return ResponseEntity.ok(postService.getPublishedPosts());
	}

	@GetMapping("/slug/{slug}")
	public ResponseEntity<PostResponseDTO> getPostBySlug(@PathVariable String slug,
			@RequestParam(name = "userId", required = false, defaultValue = "0") int userId) {
		return ResponseEntity.ok(postService.getPostBySlug(slug, userId));
	}

	@GetMapping("/author/{authorId}")
	public ResponseEntity<List<PostResponseDTO>> getByAuthor(@PathVariable int authorId) {
		return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostResponseDTO> getById(@PathVariable int id) {
		return ResponseEntity.ok(postService.getPostById(id));
	}

	@GetMapping("/category/{catId}")
	public ResponseEntity<List<PostResponseDTO>> getByCategoryId(@PathVariable Integer catId) {
		return ResponseEntity.ok(postService.getPostsByCategoryId(catId));
	}

	@PostMapping("/{postId}/like")
	public ResponseEntity<Void> incrementLikes(@PathVariable int postId, @RequestParam(name = "userId") int userId) {
		postService.incrementLikes(postId, userId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable int postId) {
		postService.deletePost(postId);
		return ResponseEntity.noContent().build();
	}

    // Saved Posts (Pro Feature)
    @PostMapping("/{postId}/save")
    public ResponseEntity<Void> toggleSavePost(@PathVariable int postId, @RequestParam(name = "userId") int userId) {
        postService.toggleSavePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/saved/{userId}")
    public ResponseEntity<List<PostResponseDTO>> getSavedPosts(@PathVariable int userId) {
        return ResponseEntity.ok(postService.getSavedPostsByUser(userId));
    }

    @GetMapping("/{postId}/is-saved")
    public ResponseEntity<Boolean> isPostSaved(@PathVariable int postId, @RequestParam(name = "userId") int userId) {
        return ResponseEntity.ok(postService.isPostSavedByUser(postId, userId));
    }
}