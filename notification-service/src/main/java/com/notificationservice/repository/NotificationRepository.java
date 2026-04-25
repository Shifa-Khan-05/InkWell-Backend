package com.notificationservice.repository;

import com.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);
    List<Notification> findByRecipientIdAndIsRead(Integer recipientId, boolean isRead);
    long countByRecipientIdAndIsRead(Integer recipientId, boolean isRead);
}