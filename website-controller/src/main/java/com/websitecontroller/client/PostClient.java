package com.websitecontroller.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.websitecontroller.dto.PostResponseDTO;

@FeignClient(name = "POST-SERVICE")
public interface PostClient {
	@GetMapping("/posts/published")
	List<PostResponseDTO> getPublishedPosts();

	@GetMapping("/posts/slug/{slug}")
	PostResponseDTO getPostBySlug(@PathVariable String slug, @RequestParam int userId);

	@GetMapping("/posts/author/{authorId}")
	List<PostResponseDTO> getPostsByAuthor(@PathVariable("authorId") int authorId);
}