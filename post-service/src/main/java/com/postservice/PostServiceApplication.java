package com.postservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients; // ✅ Import this

@SpringBootApplication(exclude = { 
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class 
})
@EnableDiscoveryClient
@EnableFeignClients 
@EnableCaching
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.postservice.repository")
public class PostServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostServiceApplication.class, args);
    }
}