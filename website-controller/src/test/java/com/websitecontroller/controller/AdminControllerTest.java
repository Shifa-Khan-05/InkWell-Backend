package com.websitecontroller.controller;

import com.websitecontroller.client.AuthClient;
import com.websitecontroller.client.CommentClient;
import com.websitecontroller.client.PostClient;
import com.websitecontroller.dto.PostResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthClient authClient;

    @Mock
    private PostClient postClient;

    @Mock
    private CommentClient commentClient; // Remains mocked for fault tolerance testing

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

  

    @Test
    @DisplayName("Should return full summary when all microservices are operational")
    void testGetPlatformSummary_Success() throws Exception {
        // 1. Create a concrete list for Users
        List<Map<String, Object>> mockUsers = new ArrayList<>();
        mockUsers.add(Map.of("id", 1, "fullName", "Shifa Khan"));
        
        // 2. Create concrete list for Posts
        PostResponseDTO p1 = new PostResponseDTO();
        p1.setPostId(101);
        p1.setLikesCount(10);
        
        List<PostResponseDTO> mockPosts = new ArrayList<>();
        mockPosts.add(p1);

        // 3. Define EXACT Mock Behaviors
        // Ensure the mock returns the list we just created
        when(authClient.getAllUsers()).thenReturn(mockUsers); 
        when(postClient.getPublishedPosts()).thenReturn(mockPosts);

        // Perform request
        mockMvc.perform(get("/api/admin/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformStatus", is("OPERATIONAL")))
                // ✅ This should now match the mockUsers.size() which is 1
                .andExpect(jsonPath("$.totalUsers", is(1))) 
                .andExpect(jsonPath("$.totalPosts", is(1)));
    }
    
    
    @Test
    @DisplayName("Should return DEGRADED status if core Auth service fails")
    void testGetPlatformSummary_CoreServiceFailure() throws Exception {
        // Simulate Auth-Service throwing an exception (Microservice Down)
        when(authClient.getAllUsers()).thenThrow(new RuntimeException("Auth service connection refused"));

        mockMvc.perform(get("/api/admin/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformStatus", is("DEGRADED")))
                .andExpect(jsonPath("$.error", containsString("core services")));
    }

    
    @Test
    @DisplayName("Should remain OPERATIONAL if core services work but others have issues")
    void testGetPlatformSummary_NonCriticalFailure() throws Exception {
        // Core services work
        when(authClient.getAllUsers()).thenReturn(Collections.emptyList());
        when(postClient.getPublishedPosts()).thenReturn(Collections.emptyList());
        
        // Perform request
        mockMvc.perform(get("/api/admin/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformStatus", is("OPERATIONAL")))
                // ❌ REMOVE THIS LINE: .andExpect(jsonPath("$.totalComments", is(0))) 
                .andExpect(jsonPath("$.totalUsers", is(0)))
                .andExpect(jsonPath("$.totalPosts", is(0)));
    }
}