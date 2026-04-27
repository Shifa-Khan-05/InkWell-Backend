package com.websitecontroller.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.websitecontroller.dto.CommentResponseDTO;

@FeignClient(name = "COMMENT-SERVICE")
public interface CommentClient {
    @GetMapping("/comments/post/{postId}")
    List<CommentResponseDTO> getCommentsByPost(@PathVariable Integer postId);
}