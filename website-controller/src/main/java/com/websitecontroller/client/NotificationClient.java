package com.websitecontroller.client;

import com.websitecontroller.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    @GetMapping("/notifications/user/{recipientId}")
    List<NotificationDTO> getNotificationsForUser(@PathVariable("recipientId") int recipientId);
}