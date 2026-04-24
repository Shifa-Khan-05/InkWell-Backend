package com.newsletterservice.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;
import com.newsletterservice.service.NewsletterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

class NewsletterServiceTest {

    @Mock
    private SubscriberRepository repository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NewsletterServiceImpl newsletterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubscribe_NewUser() {
        // Arrange
        String email = "shifa@example.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        newsletterService.subscribe(email);

        // Assert
        verify(repository, times(1)).save(any(Subscriber.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendBulkEmail_OnlyActiveSubscribers() {
        // Arrange
        Subscriber activeSub = new Subscriber();
        activeSub.setEmail("active@inkwell.com");
        activeSub.setStatus("ACTIVE");

        // Mock repository to return only the active user
        when(repository.findByStatus("ACTIVE")).thenReturn(Arrays.asList(activeSub));

        // Act
        newsletterService.sendBulkEmail("News", "Content");

        // Assert
        verify(repository, times(1)).findByStatus("ACTIVE");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testUnsubscribe() {
        // Arrange
        String email = "user@test.com";
        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(email);
        subscriber.setStatus("ACTIVE");
        
        when(repository.findByEmail(email)).thenReturn(Optional.of(subscriber));

        // Act
        newsletterService.unsubscribe(email);

        // Assert
        assertEquals("UNSUBSCRIBED", subscriber.getStatus());
        verify(repository, times(1)).save(subscriber);
    }
}