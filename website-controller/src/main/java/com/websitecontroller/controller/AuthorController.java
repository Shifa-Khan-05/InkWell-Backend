package com.websitecontroller.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.websitecontroller.client.NotificationClient;
import com.websitecontroller.client.PostClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/author")
@RequiredArgsConstructor
public class AuthorController {

    private final PostClient postClient;
    private final NotificationClient notificationClient;

    @GetMapping("/dashboard/{authorId}")
    public ResponseEntity<Map<String, Object>> getDashboardData(@PathVariable int authorId) {
        Map<String, Object> dashboard = new HashMap<>();

        // Assistant logic: Go get the posts
        dashboard.put("myPosts", postClient.getPostsByAuthor(authorId));

        // Assistant logic: Go get the alerts
        dashboard.put("notifications", notificationClient.getNotificationsForUser(authorId));

        return ResponseEntity.ok(dashboard);
    }
}