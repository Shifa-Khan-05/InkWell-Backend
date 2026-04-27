package com.notificationservice.service;

import com.notificationservice.client.AuthClient;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {

	private final NotificationRepository repository;
	private final JavaMailSender mailSender;
	private final AuthClient authClient;

	// Create In-App Notification
	public Notification createNotification(Integer recipientId, Integer actorId, String type, String msg,
			Integer relatedId) {
		Notification note = new Notification();
		note.setRecipientId(recipientId);
		note.setActorId(actorId);
		note.setType(type);
		note.setMessage(msg);
		note.setRelatedId(relatedId);

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

	public void markAllAsRead(Integer recipientId) {
		List<Notification> unread = repository.findByRecipientIdAndIsRead(recipientId, false);
		unread.forEach(n -> n.setRead(true));
		repository.saveAll(unread);
	}

	public void deleteReadNotifications(Integer recipientId) {
		List<Notification> readNotes = repository.findByRecipientIdAndIsRead(recipientId, true);
		repository.deleteAll(readNotes);
	}

	public void sendStyledEmail(String recipientEmail, String subject, String title, String body, String actionUrl) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			String htmlContent = """
					<!DOCTYPE html>
					<html>
					<head>
					    <style>
					        .container { font-family: 'Segoe UI', sans-serif; max-width: 600px; margin: auto; background-color: #0f172a; color: #ffffff; border-radius: 16px; overflow: hidden; border: 1px solid #1e293b; }
					        .header { padding: 40px 20px; text-align: center; background-color: #1e293b; border-bottom: 1px solid #334155; }
					        .logo { font-size: 32px; font-weight: bold; color: #f59e0b; text-transform: uppercase; letter-spacing: 2px; }
					        .badge { background-color: #065f46; color: #34d399; padding: 6px 16px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-bottom: 20px; display: inline-block; }
					        .content { padding: 50px 40px; text-align: center; }
					        .content h1 { font-size: 28px; margin-bottom: 20px; color: #f8fafc; }
					        .content p { color: #94a3b8; line-height: 1.8; margin-bottom: 35px; font-size: 16px; }
					        .button { background-color: #f59e0b; color: #0f172a; padding: 16px 32px; border-radius: 12px; text-decoration: none; font-weight: 800; display: inline-block; transition: background 0.3s; }
					        .footer { padding: 30px; text-align: center; font-size: 11px; color: #64748b; background-color: #020617; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <div class="logo">INKWELL</div>
					        </div>
					        <div class="content">
					            <div class="badge">PLATFORM ALERT</div>
					            <p>Hi there,</p>
					            <h1>[[${title}]]</h1>
					            <p>[[${message}]]</p>
					            <a href="[[${actionUrl}]]" class="button">VIEW IN WORKSPACE</a>
					        </div>
					        <div class="footer">
					            Sent by InkWell Microservices Framework<br>
					            Bhopal, India &bull; 2026
					        </div>
					    </div>
					</body>
					</html>
					""";

			// Dynamic Data Replacement
			htmlContent = htmlContent.replace("[[${title}]]", title);
			htmlContent = htmlContent.replace("[[${message}]]", body);
			htmlContent = htmlContent.replace("[[${actionUrl}]]", actionUrl);

			helper.setTo(recipientEmail);
			helper.setSubject("InkWell | " + subject);
			helper.setText(htmlContent, true);
			helper.setFrom("alerts@inkwell.com");

			mailSender.send(message);
		} catch (Exception e) {
			System.err.println("Styled Email Error: " + e.getMessage());
		}
	}

	public void sendBulkNotification(String message) {
		try {
			// 1. Fetch all user IDs from Auth-Service via Feign
			List<Integer> allUserIds = authClient.getAllUserIds();

			// 2. Prepare a list of notification entities
			List<Notification> bulkNotes = allUserIds.stream().map(userId -> {
				Notification note = new Notification();
				note.setRecipientId(userId);
				note.setActorId(0); // 0 represents 'SYSTEM'
				note.setType("SYSTEM_ALERT");
				note.setMessage(message);
				note.setRelatedId(0);
				note.setRead(false);
				return note;
			}).toList();

			// 3. Batch save to the database for high performance
			repository.saveAll(bulkNotes);

			System.out.println("Bulk notification dispatched to " + allUserIds.size() + " users.");
		} catch (Exception e) {
			System.err.println("Failed to send bulk notification: " + e.getMessage());
		}
	}
}