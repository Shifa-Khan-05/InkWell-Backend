package com.postservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.postservice.dto.UserResponseDTO; // Ensure you have this local DTO now

@FeignClient(name = "AUTH-SERVICE") // Case-sensitive; must match Eureka name [cite: 727]
public interface AuthClient {

    @GetMapping("/auth/profile/{userId}")
    UserResponseDTO getUserById(@PathVariable("userId") int userId);
}