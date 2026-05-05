package com.notificationservice.service;

import com.notificationservice.client.AuthClient;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setNotificationId(1L);
        notification.setRecipientId(1);
        notification.setRead(false);
    }

    @Test
    void createNotification_Success() {
        when(repository.save(any(Notification.class))).thenReturn(notification);
        
        Notification result = notificationService.createNotification(1, 2, "TYPE", "Message", 100);
        
        assertThat(result).isNotNull();
    }

    @Test
    void getNotificationsForUser_Success() {
        when(repository.findByRecipientIdOrderByCreatedAtDesc(1)).thenReturn(List.of(notification));
        assertThat(notificationService.getNotificationsForUser(1)).hasSize(1);
    }

    @Test
    void markAsRead_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        notificationService.markAsRead(1L);
        assertThat(notification.isRead()).isTrue();
        verify(repository).save(notification);
    }

    @Test
    void getUnreadCount_Success() {
        when(repository.countByRecipientIdAndIsRead(1, false)).thenReturn(5L);
        assertThat(notificationService.getUnreadCount(1)).isEqualTo(5L);
    }

    @Test
    void markAllAsRead_Success() {
        when(repository.findByRecipientIdAndIsRead(1, false)).thenReturn(List.of(notification));
        notificationService.markAllAsRead(1);
        assertThat(notification.isRead()).isTrue();
        verify(repository).saveAll(anyList());
    }

    @Test
    void deleteReadNotifications_Success() {
        when(repository.findByRecipientIdAndIsRead(1, true)).thenReturn(List.of(notification));
        notificationService.deleteReadNotifications(1);
        verify(repository).deleteAll(anyList());
    }

    @Test
    void sendStyledEmail_Success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        notificationService.sendStyledEmail("test@test.com", "Sub", "Title", "Body", "url");
        
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void markAsRead_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        notificationService.markAsRead(1L);
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void sendStyledEmail_Failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server down"));
        // Should catch and log error, not throw
        notificationService.sendStyledEmail("test@test.com", "Sub", "Title", "Body", "url");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendBulkNotification_Success() {
        when(authClient.getAllUserIds()).thenReturn(List.of(1, 2, 3));
        notificationService.sendBulkNotification("System Message");
        verify(repository).saveAll(anyIterable());
    }

    @Test
    void sendBulkNotification_Failure() {
        when(authClient.getAllUserIds()).thenThrow(new RuntimeException("Auth service down"));
        // Should catch and log error, not throw
        notificationService.sendBulkNotification("System Message");
        verify(repository, never()).saveAll(anyIterable());
    }
}
