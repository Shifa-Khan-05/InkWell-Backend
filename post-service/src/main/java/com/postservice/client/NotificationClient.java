package com.postservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient { // ✅ MUST BE 'interface', NOT 'class'

    @PostMapping("/notifications/send")
    void sendNotification(@RequestBody Map<String, Object> notificationData);
}