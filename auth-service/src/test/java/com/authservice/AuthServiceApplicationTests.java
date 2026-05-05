package com.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
		assertNotNull(this, "The application context should load correctly.");
	}

}
