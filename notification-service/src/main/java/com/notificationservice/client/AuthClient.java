package com.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {
    // This should return a list of all user IDs from your Auth database
    @GetMapping("/auth/users/ids")
    List<Integer> getAllUserIds();
}