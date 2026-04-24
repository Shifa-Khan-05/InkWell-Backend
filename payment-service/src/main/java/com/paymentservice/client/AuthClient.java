package com.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {
    @PutMapping("/auth/users/{id}/upgrade")
    void upgradeUser(@PathVariable("id") Integer userId);
}