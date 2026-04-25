package com.notificationservice.service;

import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {

	private final NotificationRepository repository;
	private final JavaMailSender mailSender;

	// Create In-App Notification
	public Notification createNotification(Integer recipientId, Integer actorId, String type, String msg,
			Integer relatedId) {
		Notification note = new Notification();
		note.setRecipientId(recipientId);
		note.setActorId(actorId);
		note.setType(type);
		note.setMessage(msg);
		note.setRelatedId(relatedId);

		// Trigger Email logic here if needed
		// sendEmailNotification(recipientId, msg);

		return repository.save(note);
	}

	public List<Notification> getNotificationsForUser(Integer recipientId) {
		return repository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
	}

	public void markAsRead(Long notificationId) {
		repository.findById(notificationId).ifPresent(n -> {
			n.setRead(true);
			repository.save(n);
		});
	}

	public long getUnreadCount(Integer recipientId) {
		return repository.countByRecipientIdAndIsRead(recipientId, false);
	}

	// ✅ Add these to your Service Implementation

	// Mark all notifications for a specific user as read
	public void markAllAsRead(Integer recipientId) {
		List<Notification> unread = repository.findByRecipientIdAndIsRead(recipientId, false);
		unread.forEach(n -> n.setRead(true));
		repository.saveAll(unread);
	}

	// Delete all notifications that have already been read (Cleanup)
	public void deleteReadNotifications(Integer recipientId) {
		List<Notification> readNotes = repository.findByRecipientIdAndIsRead(recipientId, true);
		repository.deleteAll(readNotes);
	}

	// Send a broadcast message to all users (System Alert)
	public void sendBulkNotification(String message) {
		// Logic to fetch all user IDs (via AuthClient) and loop
		// Or just create one record with recipientId = 0 if frontend handles system
		// alerts
	}
}