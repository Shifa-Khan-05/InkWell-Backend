package com.notificationservice.consumer;

import com.notificationservice.service.NotificationServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
public class PostEventConsumer {

	@Autowired
	private NotificationServiceImpl notificationService;

	@Autowired
	private com.notificationservice.client.AuthClient authClient;

	@org.springframework.beans.factory.annotation.Value("${FRONTEND_URL:https://inkwell-blogging.netlify.app}")
	private String frontendUrl;

	@RabbitListener(queuesToDeclare = @org.springframework.amqp.rabbit.annotation.Queue("post_notification_queue"))
	public void consumePostMessage(Map<String, Object> message) {
		log.info("Received message from RabbitMQ: {}", message);

        try {
            Integer recipientId = (Integer) message.get("recipientId");
            String type = (String) message.get("type");
            String title = (String) message.get("title");
            Integer postId = (Integer) message.get("postId");

            if (recipientId == null || type == null) {
                log.warn("Cannot send notification: Invalid message payload.");
                return;
            }

            Map<String, Object> user = authClient.getUserById(recipientId);
            String email = (user != null) ? (String) user.get("email") : null;

            String actionUrl = frontendUrl + "/dashboard";
            
            if ("LIKE".equalsIgnoreCase(type)) {
                notificationService.createNotification(recipientId, 0, type, "Someone liked your post: " + title, postId);
                if (email != null) {
                    notificationService.sendStyledEmail(email, "New Engagement on Your Post", "You got a like! \uD83D\uDC96", "Someone just liked your manuscript: " + title, actionUrl);
                }
            } else if ("COMMENT".equalsIgnoreCase(type)) {
                notificationService.createNotification(recipientId, 0, type, "Someone commented on your post: " + title, postId);
                if (email != null) {
                    notificationService.sendStyledEmail(email, "New Discussion on Your Post", "You got a comment! \uD83D\uDDE8\uFE0F", "Someone just commented on your manuscript: " + title, actionUrl);
                }
            } else if ("NEW_POST".equalsIgnoreCase(type)) {
                notificationService.createNotification(recipientId, 0, type, "Your post '" + title + "' is published.", postId);
                if (email != null) {
                    notificationService.sendStyledEmail(email, "Manuscript Published", "Successfully Published", "Your post '" + title + "' is now live for readers.", actionUrl);
                }
            }

        } catch (Exception e) {
            log.error("Error processing RabbitMQ message: {}", e.getMessage());
        }
	}
}