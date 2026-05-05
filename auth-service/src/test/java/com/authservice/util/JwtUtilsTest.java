package com.authservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "mysecretkeymysecretkeymysecretkeymysecretkeymysecretkey");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);
    }

    @Test
    void generateToken_Success() {
        String token = jwtUtils.generateToken("test@test.com");
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }
}
