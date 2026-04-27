package com.authservice.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {
	@PostMapping("/notifications/send-styled-email")
	void sendStyledEmail(@RequestBody Map<String, String> emailRequest);
}