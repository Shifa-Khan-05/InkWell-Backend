package com.newsletterservice.service;

import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceImplTest {

    @Mock
    private SubscriberRepository repository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NewsletterServiceImpl newsletterService;

    private Subscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new Subscriber();
        subscriber.setEmail("test@test.com");
        subscriber.setStatus("ACTIVE");
    }

    @Test
    void subscribe_NewEmail_Success() {
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        
        newsletterService.subscribe("test@test.com");
        
        verify(repository).save(any(Subscriber.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void subscribe_ExistingEmail_DoesNothing() {
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(subscriber));
        
        newsletterService.subscribe("test@test.com");
        
        verify(repository, never()).save(any(Subscriber.class));
    }

    @Test
    void unsubscribe_Success() {
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(subscriber));
        
        newsletterService.unsubscribe("test@test.com");
        
        verify(repository).save(argThat(s -> s.getStatus().equals("UNSUBSCRIBED")));
    }

    @Test
    void sendBulkEmail_Success() {
        when(repository.findByStatus("ACTIVE")).thenReturn(List.of(subscriber));
        
        newsletterService.sendBulkEmail("Subject", "Body");
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDirectEmail_Success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        newsletterService.sendDirectEmail("test@test.com", "Subject", "Title", "Body");
        
        verify(mailSender).send(mimeMessage);
    }
}
