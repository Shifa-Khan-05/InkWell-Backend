package com.newsletterservice.service; 

import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

    private final SubscriberRepository repository;
    private final JavaMailSender mailSender;

    @Override
    public void subscribe(String email) {
        // Only proceed if email isn't already in the system
        if (repository.findByEmail(email).isEmpty()) {
            String token = UUID.randomUUID().toString();
            
            Subscriber subscriber = new Subscriber();
            subscriber.setEmail(email);
            subscriber.setVerificationToken(token);
            subscriber.setStatus("PENDING"); // ✅ Set to Pending initially
            repository.save(subscriber);

            // Send Confirmation Link (Double Opt-In)
            String confirmLink = "http://localhost:8080/newsletter/confirm?token=" + token;
            sendEmail(email, "Confirm your InkWell Subscription",
                    "Welcome to InkWell! To start receiving our narratives, please confirm your subscription by clicking here: " + confirmLink);
        }
    }

    @Override
    public void unsubscribe(String email) {
        repository.findByEmail(email).ifPresent(s -> {
            s.setStatus("UNSUBSCRIBED");
            repository.save(s);
        });
    }

    @Override
    public void sendBulkEmail(String subject, String body) {
        // ✅ Only fetch users who have completed the Double Opt-In
        List<Subscriber> activeSubscribers = repository.findByStatus("ACTIVE");
        
        for (Subscriber s : activeSubscribers) {
            try {
                sendEmail(s.getEmail(), subject, body);
            } catch (Exception e) {
                System.err.println("Failed to send broadcast to: " + s.getEmail());
            }
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("shifa.inkwell.official@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}