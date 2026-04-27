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

	@PostMapping("/send")
	public ResponseEntity<Notification> send(@RequestBody Notification note) {
		return ResponseEntity.ok(notificationService.createNotification(note.getRecipientId(), note.getActorId(),
				note.getType(), note.getMessage(), note.getRelatedId()));
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