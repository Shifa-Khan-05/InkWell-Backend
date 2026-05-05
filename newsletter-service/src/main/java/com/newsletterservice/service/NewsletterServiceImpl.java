package com.newsletterservice.service;

import com.newsletterservice.entity.Subscriber;
import com.newsletterservice.repository.SubscriberRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
			String confirmLink = "http://localhost:8081/newsletter/confirm?token=" + token;
			sendEmail(email, "Confirm your InkWell Subscription",
					"Welcome to InkWell! To start receiving our narratives, please confirm your subscription by clicking here: "
							+ confirmLink);
		}
	}

	private void sendStyledNewsletterEmail(String recipientEmail, String subject, String title, String body,
			String actionUrl) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			String htmlContent = """
					<!DOCTYPE html>
					<html>
					<head>
					    <style>
					        .container { font-family: 'Segoe UI', sans-serif; max-width: 600px; margin: auto; background-color: #0f172a; color: #ffffff; border-radius: 16px; overflow: hidden; border: 1px solid #1e293b; }
					        .header { padding: 40px 20px; text-align: center; background-color: #1e293b; }
					        .logo { font-size: 32px; font-weight: bold; color: #f59e0b; text-transform: uppercase; letter-spacing: 2px; }
					        .badge { background-color: #1e3a8a; color: #60a5fa; padding: 6px 16px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-bottom: 20px; display: inline-block; }
					        .content { padding: 50px 40px; text-align: center; }
					        .content h1 { font-size: 28px; margin-bottom: 20px; color: #f8fafc; }
					        .content p { color: #94a3b8; line-height: 1.8; margin-bottom: 35px; font-size: 16px; }
					        .button { background-color: #f59e0b; color: #0f172a; padding: 16px 32px; border-radius: 12px; text-decoration: none; font-weight: 800; display: inline-block; }
					        .footer { padding: 30px; text-align: center; font-size: 11px; color: #64748b; background-color: #020617; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header"><div class="logo">INKWELL</div></div>
					        <div class="content">
					            <div class="badge">NEWSLETTER OPT-IN</div>
					            <h1>[[${title}]]</h1>
					            <p>[[${message}]]</p>
					            <a href="[[${actionUrl}]]" class="button">CONFIRM SUBSCRIPTION</a>
					        </div>
					        <div class="footer">
					            InkWell Literary Framework &bull; Bhopal, India<br>
					            If you didn't request this, you can safely ignore this email.
					        </div>
					    </div>
					</body>
					</html>
					""";

			htmlContent = htmlContent.replace("[[${title}]]", title);
			htmlContent = htmlContent.replace("[[${message}]]", body);
			htmlContent = htmlContent.replace("[[${actionUrl}]]", actionUrl);

			helper.setTo(recipientEmail);
			helper.setSubject("InkWell | " + subject);
			helper.setText(htmlContent, true);
			helper.setFrom("newsletter@inkwell.com");

			mailSender.send(message);
		} catch (Exception e) {
			System.err.println("Newsletter Email Error: " + e.getMessage());
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
	
	@Override
	public void sendDirectEmail(String email, String subject, String title, String body) {
	    // We reuse the styled template we already created
	    sendStyledNewsletterEmail(
	        email, 
	        subject, 
	        title, 
	        body, 
	        "http://localhost:5173/dashboard" // Default action link
	    );
	    System.out.println("Direct dispatch completed for: " + email);
	}
}