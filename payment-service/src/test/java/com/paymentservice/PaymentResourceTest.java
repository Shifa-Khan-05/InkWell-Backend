package com.paymentservice;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.paymentservice.controller.PaymentResource;
import com.paymentservice.client.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

class PaymentResourceTest {

	@Mock
	private AuthClient authClient;

	@InjectMocks
	private PaymentResource paymentResource;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testVerifyPayment_Success() {
		// Arrange
		Map<String, String> payload = new HashMap<>();
		payload.put("razorpay_payment_id", "pay_test_123");
		payload.put("razorpay_signature", "sig_abc_789");
		payload.put("userId", "21");

		// Act
		ResponseEntity<?> response = paymentResource.verifyPayment(payload);

		// Assert
		// ✅ Use .value() to compare Integer to Integer
		assertEquals(200, response.getStatusCode().value());
		verify(authClient, times(1)).upgradeUser(21);
	}

	@Test
	void testVerifyPayment_MissingUserId() {
		// Arrange
		Map<String, String> payload = new HashMap<>();
		payload.put("userId", "null");

		// Act
		ResponseEntity<?> response = paymentResource.verifyPayment(payload);

		// Assert
		// ✅ Alternatively, compare HttpStatus objects directly
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(authClient, never()).upgradeUser(anyInt());
	}
}