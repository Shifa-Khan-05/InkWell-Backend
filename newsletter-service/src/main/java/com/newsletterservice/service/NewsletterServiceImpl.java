package com.newsletterservice.service; 
import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

	private final SubscriberRepository repository;
	private final JavaMailSender mailSender;

	@Override
	public void subscribe(String email) {
		if (repository.findByEmail(email).isEmpty()) {
			Subscriber subscriber = new Subscriber();
			subscriber.setEmail(email);
			repository.save(subscriber);

			// Send Welcome Email
			sendEmail(email, "Welcome to InkWell!",
					"Greetings from InkWell! Thank you for subscribing to our narrative journey. Stay tuned for fresh stories.");
		}
	}

	@Override
	public void unsubscribe(String email) {
		repository.findByEmail(email).ifPresent(s -> {
			s.setActive(false);
			repository.save(s);
		});
	}

	@Override
	public void sendBulkEmail(String subject, String body) {
		List<Subscriber> activeSubscribers = repository.findByActiveTrue();
		for (Subscriber s : activeSubscribers) {
			try {
				sendEmail(s.getEmail(), subject, body);
			} catch (Exception e) {
				// Log the error for this specific email but continue the loop
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