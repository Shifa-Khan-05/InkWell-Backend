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

	@RabbitListener(queuesToDeclare = @org.springframework.amqp.rabbit.annotation.Queue("post_notification_queue"))
	public void consumePostMessage(Map<String, Object> message) {
		log.info("Received message from RabbitMQ: {}", message.get("title"));

		// Create in-app notification logic
		notificationService.createNotification((Integer) message.get("authorId"), 0, // System Actor
				"POST_CREATED", "Success! Your post '" + message.get("title") + "' is ready for review.",
				(Integer) message.get("postId"));
	}
}