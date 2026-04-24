package com.paymentservice.controller;

import java.util.Map;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.paymentservice.client.AuthClient;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/payments")
public class PaymentResource {

	@Value("${razorpay.key.id}")
	private String keyId;

	@Value("${razorpay.key.secret}")
	private String keySecret;

	@Autowired
	private AuthClient authClient;

	@PostMapping("/create-order")
	public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
		try {
			RazorpayClient client = new RazorpayClient(keyId, keySecret);

			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", 49900); // ₹499 in paise
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

			Order order = client.orders.create(orderRequest);

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> orderMap = mapper.readValue(order.toString(), Map.class);
			return ResponseEntity.ok(orderMap);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Order creation failed");
		}
	}
	@PostMapping("/verify")
	public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
	    try {
	        String paymentId = data.get("razorpay_payment_id");
	        String signature = data.get("razorpay_signature");
	        String userIdStr = data.get("userId");

	        // 1. Log to see exactly what is arriving in Eclipse console
	        System.out.println("Processing payment for user: " + userIdStr);

	        // 2. Safety check for the userId
	        if (userIdStr == null || userIdStr.equals("null")) {
	            return ResponseEntity.badRequest().body("User ID is missing");
	        }

	        Integer userId = Integer.parseInt(userIdStr);

	        // 3. Upgrade user via Feign Client
	        // Note: If Auth-Service has security, this might fail with 401/403
	        try {
	            authClient.upgradeUser(userId);
	        } catch (Exception feignEx) {
	            System.err.println("Feign call to Auth-Service failed: " + feignEx.getMessage());
	            return ResponseEntity.status(500).body("Auth-Service unreachable or rejected request");
	        }

	        return ResponseEntity.ok(Map.of("status", "success", "message", "User upgraded"));

	    } catch (Exception e) {
	        e.printStackTrace(); // This prints the stack trace to your Eclipse console
	        return ResponseEntity.status(500).body("Internal Verification Error: " + e.getMessage());
	    }
	}
}