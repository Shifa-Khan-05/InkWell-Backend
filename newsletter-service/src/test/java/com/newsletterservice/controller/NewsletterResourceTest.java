package com.newsletterservice.controller;

import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;
import com.newsletterservice.service.NewsletterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterResourceTest {

    @Mock
    private NewsletterService newsletterService;

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private NewsletterResource newsletterResource;

    private Subscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new Subscriber();
        subscriber.setId(1L);
        subscriber.setEmail("test@test.com");
        subscriber.setStatus("PENDING");
        subscriber.setVerificationToken("token-123");
    }

    @Test
    void subscribe_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@test.com");

        doNothing().when(newsletterService).subscribe("test@test.com");

        ResponseEntity<?> response = newsletterResource.subscribe(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(newsletterService).subscribe("test@test.com");
    }

    @Test
    void broadcast_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("subject", "Subject");
        request.put("body", "Body");

        doNothing().when(newsletterService).sendBulkEmail("Subject", "Body");

        ResponseEntity<?> response = newsletterResource.broadcast(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(newsletterService).sendBulkEmail("Subject", "Body");
    }

    @Test
    void getAllSubscribers_Success() {
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber));

        ResponseEntity<List<Subscriber>> response = newsletterResource.getAllSubscribers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void confirmSubscription_ValidToken() {
        when(subscriberRepository.findByVerificationToken("token-123")).thenReturn(Optional.of(subscriber));

        ResponseEntity<String> response = newsletterResource.confirmSubscription("token-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Identity Verified");
        verify(subscriberRepository).save(subscriber);
    }

    @Test
    void confirmSubscription_InvalidToken() {
        when(subscriberRepository.findByVerificationToken("invalid")).thenReturn(Optional.empty());

        ResponseEntity<String> response = newsletterResource.confirmSubscription("invalid");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("INVALID TOKEN");
    }

    @Test
    void unsubscribe_ValidEmail() {
        when(subscriberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(subscriber));

        ResponseEntity<String> response = newsletterResource.unsubscribe("test@test.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Connections Severed");
        verify(subscriberRepository).delete(subscriber);
    }

    @Test
    void unsubscribe_InvalidEmail() {
        when(subscriberRepository.findByEmail("invalid@test.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = newsletterResource.unsubscribe("invalid@test.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("EMAIL NOT FOUND");
    }

    @Test
    void sendDirectMail_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@test.com");
        request.put("subject", "Subject");
        request.put("title", "Title");
        request.put("body", "Body");

        doNothing().when(newsletterService).sendDirectEmail("test@test.com", "Subject", "Title", "Body");

        ResponseEntity<?> response = newsletterResource.sendDirectMail(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(newsletterService).sendDirectEmail("test@test.com", "Subject", "Title", "Body");
    }

    @Test
    void sendDirectMail_MissingEmail() {
        Map<String, String> request = new HashMap<>();
        request.put("subject", "Subject");

        ResponseEntity<?> response = newsletterResource.sendDirectMail(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
