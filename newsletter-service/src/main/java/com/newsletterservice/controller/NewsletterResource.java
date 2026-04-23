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

            // ✅ Attractive HTML Response
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
                        <a href="http://localhost:5173/dashboard" 
                           class="inline-block w-full bg-white text-black font-black uppercase italic py-5 rounded-2xl hover:bg-green-500 hover:text-white transition-all duration-300 shadow-xl">
                            Enter Dashboard
                        </a>
                    </div>
                </body>
                </html>
                """;
            return ResponseEntity.ok().header("Content-Type", "text/html").body(successHtml);
        }
        
        // Attractive Error Page if token is invalid
        String errorHtml = """
            <body style="background:#000; color:#fff; display:flex; justify-content:center; align-items:center; height:100vh; font-family:sans-serif;">
                <div style="text-align:center; border:1px solid #333; padding:40px; border-radius:30px;">
                    <h2 style="color:#ef4444; font-weight:900;">INVALID TOKEN</h2>
                    <p style="color:#666;">This verification link has expired or is no longer valid.</p>
                    <a href="http://localhost:5173/login" style="color:#3b82f6; text-decoration:none;">Return to Login</a>
                </div>
            </body>
            """;
        return ResponseEntity.status(400).header("Content-Type", "text/html").body(errorHtml);
    }
}