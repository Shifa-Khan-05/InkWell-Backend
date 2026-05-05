package com.paymentservice.controller;

import com.paymentservice.client.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentResourceTest {

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private PaymentResource paymentResource;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentResource, "keyId", "test_id");
        ReflectionTestUtils.setField(paymentResource, "keySecret", "test_secret");
    }

    @Test
    void verifyPayment_Success() {
        Map<String, String> data = new HashMap<>();
        data.put("razorpay_payment_id", "pay_123");
        data.put("razorpay_signature", "sig_123");
        data.put("userId", "1");

        // ✅ FIXED: Use doNothing() for void methods
        doNothing().when(authClient).upgradeUser(1);

        ResponseEntity<?> response = paymentResource.verifyPayment(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authClient, times(1)).upgradeUser(1);
    }

    @Test
    void verifyPayment_AuthClientFails() {
        Map<String, String> data = new HashMap<>();
        data.put("razorpay_payment_id", "pay_123");
        data.put("razorpay_signature", "sig_123");
        data.put("userId", "1");

        // ✅ FIXED: Use doThrow() for void methods
        doThrow(new RuntimeException("Feign error")).when(authClient).upgradeUser(anyInt());

        ResponseEntity<?> response = paymentResource.verifyPayment(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void verifyPayment_MissingUserId() {
        Map<String, String> data = new HashMap<>();
        data.put("razorpay_payment_id", "pay_123");
        data.put("razorpay_signature", "sig_123");

        ResponseEntity<?> response = paymentResource.verifyPayment(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(authClient);
    }
}