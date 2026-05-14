package com.authservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("CRITICAL: Internal System Exception: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put(ERROR_KEY, "Oops! Our ink ran dry for a moment. Please try again or reach out to our support team.");
        response.put("status", "error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("SYSTEM ALERT: Runtime Exception: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        if (ex.getMessage() != null && (ex.getMessage().contains("Access is denied") || ex.getMessage().contains("Token") || ex.getMessage().contains("expired"))) {
             response.put(ERROR_KEY, "Your session has gracefully concluded. Please sign in again to continue your journey.");
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        response.put(ERROR_KEY, ex.getMessage() != null ? ex.getMessage() : "We encountered an unexpected hurdle. Please try your request again.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
