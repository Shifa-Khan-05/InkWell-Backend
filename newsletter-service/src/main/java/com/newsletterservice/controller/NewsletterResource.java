package com.newsletterservice.controller;

import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;
import com.newsletterservice.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterResource {

	private final NewsletterService newsletterService;
	private final SubscriberRepository subscriberRepository;

	@org.springframework.beans.factory.annotation.Value("${FRONTEND_URL:https://inkwell-blogging.netlify.app}")
	private String frontendUrl;

	@PostMapping("/subscribe")
	public ResponseEntity<?> subscribe(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		newsletterService.subscribe(email);
		return ResponseEntity.ok(Map.of("message", "Confirmation email sent! Please check your inbox."));
	}

	@PostMapping("/broadcast")
	public ResponseEntity<?> broadcast(@RequestBody Map<String, String> request) {
		newsletterService.sendBulkEmail(request.get("subject"), request.get("body"));
		return ResponseEntity.ok(Map.of("message", "Broadcast sent to all ACTIVE subscribers."));
	}

	@GetMapping("/all")
	public ResponseEntity<List<Subscriber>> getAllSubscribers() {
		return ResponseEntity.ok(subscriberRepository.findAll());
	}

	@GetMapping("/confirm")
	public ResponseEntity<String> confirmSubscription(@RequestParam String token) {
		Optional<Subscriber> sub = subscriberRepository.findByVerificationToken(token);

		if (sub.isPresent()) {
			Subscriber subscriber = sub.get();
			subscriber.setStatus("ACTIVE");
			subscriber.setVerificationToken(null);
			subscriberRepository.save(subscriber);

			String successHtml = """
					<!DOCTYPE html>
					<html lang="en">
					<head>
					    <meta charset="UTF-8">
					    <script src="https://cdn.tailwindcss.com"></script>
					    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;900&display=swap" rel="stylesheet">
					    <style> body { font-family: 'Inter', sans-serif; background-color: #000; } </style>
					</head>
					<body class="flex items-center justify-center min-h-screen p-6">
					    <div class="max-w-md w-full bg-gray-900 border border-gray-800 rounded-[40px] p-12 text-center shadow-2xl">
					        <div class="bg-green-500/10 w-20 h-20 rounded-3xl flex items-center justify-center mx-auto mb-8">
					            <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
					            </svg>
					        </div>
					        <h1 class="text-3xl font-black text-white italic uppercase tracking-tighter mb-4">Identity Verified</h1>
					        <p class="text-gray-400 font-medium mb-10">Subscription Activated! Welcome to the InkWell Inner Circle. Your narrative journey begins now.</p>
					        <a href="%s/dashboard"
					           class="inline-block w-full bg-white text-black font-black uppercase italic py-5 rounded-2xl hover:bg-green-500 hover:text-white transition-all duration-300 shadow-xl">
					            Enter Dashboard
					        </a>
					    </div>
					</body>
					</html>
					""";
			return ResponseEntity.ok().header("Content-Type", "text/html").body(successHtml.formatted(frontendUrl));
		}

		return ResponseEntity.status(400).header("Content-Type", "text/html")
				.body(getErrorHtml("INVALID TOKEN", "This verification link has expired or is no longer valid."));
	}

	// ✅ ADDED: Unsubscribe Endpoint
	@GetMapping("/unsubscribe")
	public ResponseEntity<String> unsubscribe(@RequestParam String email) {
		Optional<Subscriber> sub = subscriberRepository.findByEmail(email);

		if (sub.isPresent()) {
			subscriberRepository.delete(sub.get());

			String unsubscribeHtml = """
					<!DOCTYPE html>
					<html lang="en">
					<head>
					    <meta charset="UTF-8">
					    <script src="https://cdn.tailwindcss.com"></script>
					    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;900&display=swap" rel="stylesheet">
					    <style> body { font-family: 'Inter', sans-serif; background-color: #000; } </style>
					</head>
					<body class="flex items-center justify-center min-h-screen p-6">
					    <div class="max-w-md w-full bg-gray-900 border border-gray-800 rounded-[40px] p-12 text-center shadow-2xl">
					        <div class="bg-rose-500/10 w-20 h-20 rounded-3xl flex items-center justify-center mx-auto mb-8">
					            <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-rose-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M6 18L18 6M6 6l12 12" />
					            </svg>
					        </div>
					        <h1 class="text-3xl font-black text-white italic uppercase tracking-tighter mb-4">Connections Severed</h1>
					        <p class="text-gray-400 font-medium mb-10">You have been successfully removed from our manuscript dispatches. We hope our paths cross again.</p>
					        <a href="%s/browse"
					           class="inline-block w-full border border-gray-700 text-gray-400 font-bold uppercase py-4 rounded-2xl hover:bg-gray-800 hover:text-white transition-all duration-300">
					            Return to Feed
					        </a>
					    </div>
					</body>
					</html>
					""";
			return ResponseEntity.ok().header("Content-Type", "text/html").body(unsubscribeHtml.formatted(frontendUrl));
		}

		return ResponseEntity.status(404).header("Content-Type", "text/html")
				.body(getErrorHtml("EMAIL NOT FOUND", "This email is not registered in our dispatch records."));
	}

	// Helper method to keep code clean
	private String getErrorHtml(String title, String message) {
		return """
				<body style="background:#000; color:#fff; display:flex; justify-content:center; align-items:center; height:100vh; font-family:sans-serif; margin:0;">
				    <div style="text-align:center; border:1px solid #333; padding:60px; border-radius:40px; max-width:400px;">
				        <h2 style="color:#ef4444; font-weight:900; letter-spacing:-1px; text-transform:uppercase;">%s</h2>
				        <p style="color:#666; margin-bottom:30px;">%s</p>
				        <a href="%s/login" style="color:#fff; text-decoration:none; font-weight:bold; border-bottom:1px solid #fff; padding-bottom:2px;">Back to InkWell</a>
				    </div>
				</body>
				"""
				.formatted(title, message, frontendUrl);
	}

	@PostMapping("/direct-mail")
	public ResponseEntity<?> sendDirectMail(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		String subject = request.get("subject");
		String title = request.get("title");
		String body = request.get("body");

		if (email == null || email.isEmpty()) {
			return ResponseEntity.status(400).body(Map.of("error", "Recipient email is required"));
		}

		newsletterService.sendDirectEmail(email, subject, title, body);

		return ResponseEntity.ok(Map.of("message", "Direct email dispatched successfully to " + email));
	}
}