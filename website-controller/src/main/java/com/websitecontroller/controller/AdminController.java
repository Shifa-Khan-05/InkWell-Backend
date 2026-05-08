package com.websitecontroller.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.websitecontroller.client.AuthClient;
import com.websitecontroller.client.PostClient;
import com.websitecontroller.dto.PostResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

	private final AuthClient authClient;
	private final PostClient postClient;

	/**
	 * Aggregates platform-wide metrics for the Admin Dashboard. Orchestrates data
	 * from Auth and Post services.
	 */
	@GetMapping("/summary")
	public ResponseEntity<Map<String, Object>> getPlatformSummary() {
		Map<String, Object> summary = new HashMap<>();

		try {
			// 1. Fetch User Identity Metrics from Auth-Service
			List<Map<String, Object>> users = authClient.getAllUsers();
			summary.put("users", users);
			summary.put("totalUsers", users != null ? users.size() : 0);

			// 2. Fetch Content Metrics from Post-Service
			List<PostResponseDTO> posts = postClient.getPublishedPosts();
			summary.put("totalPosts", posts != null ? posts.size() : 0);

			// 3. Analytics: Identify Top 5 Performing Manuscripts (Sorted by Likes)
			if (posts != null && !posts.isEmpty()) {
				List<PostResponseDTO> topPosts = posts.stream()
						.sorted(Comparator.comparing(PostResponseDTO::getLikesCount).reversed()).limit(5)
						.collect(Collectors.toList());
				summary.put("topPosts", topPosts);
			} else {
				summary.put("topPosts", Collections.emptyList());
			}

			// 4. System Status Identification
			summary.put("platformStatus", "OPERATIONAL");

		} catch (Exception e) {
			log.error("Protocol Error: Admin summary aggregation failed: {}", e.getMessage(), e);
			summary.put("platformStatus", "DEGRADED");
			summary.put("error", "Core Services Unreachable: " + e.getMessage());

			// Return empty structures so the frontend doesn't crash
			summary.put("users", Collections.emptyList());
			summary.put("totalUsers", 0);
			summary.put("totalPosts", 0);
			summary.put("topPosts", Collections.emptyList());
		}

		return ResponseEntity.ok(summary);
	}
}