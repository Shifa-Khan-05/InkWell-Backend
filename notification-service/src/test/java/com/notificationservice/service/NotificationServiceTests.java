package com.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import com.notificationservice.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

@SpringBootTest
class NotificationServiceTests {

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private NotificationRepository repository;

    @MockBean
    private JavaMailSender mailSender; // Mocking the email sender

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndSaveNotification() {
        // Act
        Notification note = notificationService.createNotification(1, 2, "LIKE", "User 2 liked your post", 101);

        // Assert
        assertThat(note.getNotificationId()).isNotNull();
        assertThat(note.isRead()).isFalse();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetUnreadCountCorrectly() {
        // Arrange
        notificationService.createNotification(1, 2, "COMMENT", "Msg 1", 101);
        notificationService.createNotification(1, 3, "COMMENT", "Msg 2", 101);
        
        // Mark one as read manually
        Notification note = repository.findAll().get(0);
        notificationService.markAsRead(note.getNotificationId());

        // Act
        long unreadCount = notificationService.getUnreadCount(1);

        // Assert
        assertThat(unreadCount).isEqualTo(1);
    }

    @Test
    void shouldMarkAllAsRead() {
        // Arrange
        notificationService.createNotification(5, 1, "SYSTEM", "Alert 1", 0);
        notificationService.createNotification(5, 1, "SYSTEM", "Alert 2", 0);

        // Act
        notificationService.markAllAsRead(5);

        // Assert
        long unread = notificationService.getUnreadCount(5);
        assertThat(unread).isEqualTo(0);
    }

    @Test
    void shouldCleanupReadNotifications() {
        // Arrange: Create one read and one unread
        Notification readNote = notificationService.createNotification(1, 2, "TEST", "Read", 0);
        notificationService.markAsRead(readNote.getNotificationId());
        notificationService.createNotification(1, 2, "TEST", "Unread", 0);

        // Act
        notificationService.deleteReadNotifications(1);

        // Assert
        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findAll().get(0).isRead()).isFalse();
    }
}