package com.notificationservice.consumer;

import com.notificationservice.service.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostEventConsumerTest {

    @Mock
    private NotificationServiceImpl notificationService;

    @InjectMocks
    private PostEventConsumer postEventConsumer;

    @Test
    void consumePostMessage_Success() {
        Map<String, Object> message = new HashMap<>();
        message.put("title", "Test Post");
        message.put("authorId", 1);
        message.put("postId", 101);
        message.put("type", "NEW_POST");

        postEventConsumer.consumePostMessage(message);

        verify(notificationService).createNotification(
                1, 
                0, 
                "POST_CREATED", 
                "Success! Your post 'Test Post' is ready for review.", 
                101
        );
    }
}
