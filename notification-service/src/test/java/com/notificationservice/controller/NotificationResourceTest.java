package com.notificationservice.controller;

import com.notificationservice.entity.Notification;
import com.notificationservice.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationResourceTest {

    @Mock
    private NotificationServiceImpl notificationService;

    @InjectMocks
    private NotificationResource notificationResource;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setNotificationId(1L);
        notification.setRecipientId(1);
        notification.setActorId(2);
        notification.setType("LIKE");
        notification.setMessage("Test message");
        notification.setRelatedId(10);
    }

    @Test
    void send_Success() {
        when(notificationService.createNotification(1, 2, "LIKE", "Test message", 10)).thenReturn(notification);

        ResponseEntity<Notification> response = notificationResource.send(notification);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(notification);
    }

    @Test
    void getByUser_Success() {
        when(notificationService.getNotificationsForUser(1)).thenReturn(List.of(notification));

        ResponseEntity<List<Notification>> response = notificationResource.getByUser(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getCount_Success() {
        when(notificationService.getUnreadCount(1)).thenReturn(5L);

        ResponseEntity<Long> response = notificationResource.getCount(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5L);
    }

    @Test
    void markRead_Success() {
        doNothing().when(notificationService).markAsRead(1L);

        ResponseEntity<Void> response = notificationResource.markRead(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).markAsRead(1L);
    }

    @Test
    void markAllRead_Success() {
        doNothing().when(notificationService).markAllAsRead(1);

        ResponseEntity<Void> response = notificationResource.markAllRead(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).markAllAsRead(1);
    }

    @Test
    void deleteRead_Success() {
        doNothing().when(notificationService).deleteReadNotifications(1);

        ResponseEntity<Void> response = notificationResource.deleteRead(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).deleteReadNotifications(1);
    }

    @Test
    void sendBulk_Success() {
        doNothing().when(notificationService).sendBulkNotification("Bulk Message");

        ResponseEntity<Void> response = notificationResource.sendBulk("Bulk Message");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendBulkNotification("Bulk Message");
    }

    @Test
    void sendStyledEmail_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("recipientEmail", "test@test.com");
        request.put("subject", "Subject");
        request.put("title", "Title");
        request.put("body", "Body");
        request.put("actionUrl", "http://test.com");

        doNothing().when(notificationService).sendStyledEmail("test@test.com", "Subject", "Title", "Body", "http://test.com");

        ResponseEntity<Void> response = notificationResource.sendStyledEmail(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendStyledEmail("test@test.com", "Subject", "Title", "Body", "http://test.com");
    }
}
