package com.websitecontroller.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {
	@GetMapping("/auth/users")
	List<Map<String, Object>> getAllUsers(); // To list everyone in the system

	@PutMapping("/auth/users/{userId}/role")
	void changeUserRole(@PathVariable int userId, @RequestParam("newRole") String role);
}