package com.newsletterservice.service;

import java.util.List;

public interface NewsletterService {
    void subscribe(String email);
    void unsubscribe(String email);
    void sendBulkEmail(String subject, String body);
}