package com.notificationservice.consumer;

import com.notificationservice.service.NotificationServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class PostEventConsumer {

	@Autowired
	private NotificationServiceImpl notificationService;

	@RabbitListener(queues = "post_notification_queue")
	public void consumePostMessage(Map<String, Object> message) {
		System.out.println("Received message from RabbitMQ: " + message.get("title"));

		// Create in-app notification logic
		notificationService.createNotification((Integer) message.get("authorId"), 0, // System Actor
				"POST_CREATED", "Success! Your post '" + message.get("title") + "' is ready for review.",
				(Integer) message.get("postId"));
	}
}