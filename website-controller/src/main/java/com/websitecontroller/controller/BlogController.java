package com.websitecontroller.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.websitecontroller.client.CommentClient;
import com.websitecontroller.client.PostClient;
import com.websitecontroller.dto.CommentResponseDTO;
import com.websitecontroller.dto.PostResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/view")
@RequiredArgsConstructor
public class BlogController {

    private final PostClient postClient;
    private final CommentClient commentClient;

    @GetMapping("/post/{slug}")
    public ResponseEntity<Map<String, Object>> getFullPostDetails(
            @PathVariable String slug, 
            @RequestParam(defaultValue = "0") int userId) {
        
        // 1. Fetch Post Details
        PostResponseDTO post = postClient.getPostBySlug(slug, userId);
        
        // 2. Fetch related Comments
        List<CommentResponseDTO> comments = commentClient.getCommentsByPost(post.getPostId());

        // 3. Aggregate into one response
        Map<String, Object> response = new HashMap<>();
        response.put("post", post);
        response.put("comments", comments);
        
        return ResponseEntity.ok(response);
    }

    
    
}

