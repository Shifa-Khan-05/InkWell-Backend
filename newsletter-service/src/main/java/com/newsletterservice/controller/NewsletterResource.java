package com.newsletterservice.controller;

import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;
import com.newsletterservice.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterResource {

	private final NewsletterService newsletterService;
	private final SubscriberRepository subscriberRepository; // ✅ Added to fix the static reference error

	@PostMapping("/subscribe")
	public ResponseEntity<?> subscribe(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		newsletterService.subscribe(email);
		return ResponseEntity.ok(Map.of("message", "Subscription successful! Check your inbox."));
	}

	@PostMapping("/broadcast")
	public ResponseEntity<?> broadcast(@RequestBody Map<String, String> request) {
		newsletterService.sendBulkEmail(request.get("subject"), request.get("body"));
		return ResponseEntity.ok(Map.of("message", "Broadcast sent to all subscribers."));
	}

	@GetMapping("/all")
	public ResponseEntity<List<Subscriber>> getAllSubscribers() {
		return ResponseEntity.ok(subscriberRepository.findAll());
	}

}