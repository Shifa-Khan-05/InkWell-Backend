package com.commentservice.client;

import com.commentservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {
    @GetMapping("/auth/profile/{userId}")
    UserResponseDTO getUserById(@PathVariable("userId") int userId);
}