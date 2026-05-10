package com.notificationservice.controller;

import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import com.notificationservice.service.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationResource {


	private final NotificationServiceImpl notificationService;
	private final com.notificationservice.client.AuthClient authClient;

	@PostMapping("/send")
	public ResponseEntity<Notification> send(@RequestBody Notification note) {
		Notification savedNote = notificationService.createNotification(note.getRecipientId(), note.getActorId(),
				note.getType(), note.getMessage(), note.getRelatedId());
        try {
            java.util.Map<String, Object> user = authClient.getUserById(note.getRecipientId());
            String email = (String) user.get("email");
            if (email != null) {
                String subject = "InkWell Platform Alert";
                String title = "New Notification";
                if ("NEW_COMMENT".equalsIgnoreCase(note.getType())) {
                    subject = "New Discussion on Your Post";
                    title = "You got a comment! \uD83D\uDDE8\uFE0F";
                } else if ("COMMENT_APPROVED".equalsIgnoreCase(note.getType())) {
                    subject = "Discussion Approved";
                    title = "Comment Live \uD83C\uDF89";
                } else if ("ROLE_REQUEST".equalsIgnoreCase(note.getType())) {
                    subject = "Role Upgrade Request";
                    title = "Upgrade Protocol Initiated";
                } else if ("ROLE_APPROVED".equalsIgnoreCase(note.getType())) {
                    subject = "Role Request Approved";
                    title = "Upgrade Successful \uD83C\uDF89";
                } else if ("ROLE_REJECTED".equalsIgnoreCase(note.getType())) {
                    subject = "Role Request Update";
                    title = "Upgrade Declined";
                } else if ("ROLE_UPDATE".equalsIgnoreCase(note.getType())) {
                    subject = "InkWell Account Update";
                    title = "Credential Status Change";
                }
                notificationService.sendStyledEmail(email, subject, title, note.getMessage(), "https://inkwell-blogging.netlify.app/dashboard");
            }
        } catch (Exception e) {
            System.err.println("Failed to send email from controller: " + e.getMessage());
        }
		return ResponseEntity.ok(savedNote);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Notification>> getByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
	}

	@GetMapping("/unread-count/{userId}")
	public ResponseEntity<Long> getCount(@PathVariable Integer userId) {
		return ResponseEntity.ok(notificationService.getUnreadCount(userId));
	}

	@PutMapping("/{id}/read")
	public ResponseEntity<Void> markRead(@PathVariable Long id) {
		notificationService.markAsRead(id);
		return ResponseEntity.ok().build();
	}

	// ✅ Add these endpoints

	@PutMapping("/user/{userId}/read-all")
	public ResponseEntity<Void> markAllRead(@PathVariable Integer userId) {
		notificationService.markAllAsRead(userId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/user/{userId}/clean")
	public ResponseEntity<Void> deleteRead(@PathVariable Integer userId) {
		notificationService.deleteReadNotifications(userId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/broadcast")
	public ResponseEntity<Void> sendBulk(@RequestBody String message) {
		notificationService.sendBulkNotification(message);
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/send-styled-email")
	public ResponseEntity<Void> sendStyledEmail(@RequestBody java.util.Map<String, String> request) {
		notificationService.sendStyledEmail(
				request.get("recipientEmail"),
				request.get("subject"),
				request.get("title"),
				request.get("body"),
				request.get("actionUrl")
		);
		return ResponseEntity.ok().build();
	}
	
	
	
}