package com.authservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleException_ReturnsInternalError() {
        Exception ex = new Exception("General error");
        ResponseEntity<Map<String, String>> response = handler.handleException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", "Unable to process your request right now. Please try again.");
    }

    @Test
    void handleRuntimeException_Unauthorized() {
        RuntimeException ex = new RuntimeException("Access is denied or Token expired");
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Your session expired or you are unauthorized. Please login again.");
    }

    @Test
    void handleRuntimeException_InternalError() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", "Something went wrong");
    }
}
