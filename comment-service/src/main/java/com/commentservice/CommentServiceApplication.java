package com.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.commentservice.client.AuthClient;
import com.commentservice.client.NotificationClient;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableFeignClients(clients = {AuthClient.class, NotificationClient.class})
public class CommentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}