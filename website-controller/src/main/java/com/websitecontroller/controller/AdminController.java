package com.websitecontroller.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.websitecontroller.client.AuthClient;
import com.websitecontroller.client.CommentClient;
import com.websitecontroller.client.PostClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AuthClient authClient;
	private final PostClient postClient;
	private final CommentClient commentClient;

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Object>> getPlatformSummary() {
		Map<String, Object> summary = new HashMap<>();

		// 1. Get total user count and list (from Auth)
		summary.put("users", authClient.getAllUsers());

		// 2. Get all published posts for overview (from Post)
		summary.put("totalPosts", postClient.getPublishedPosts().size());

		// 3. You can even add logic to show system-wide stats here
		summary.put("platformStatus", "Active");

		return ResponseEntity.ok(summary);
	}
	
	
}