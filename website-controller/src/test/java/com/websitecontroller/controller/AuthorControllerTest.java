package com.websitecontroller.controller;

import com.websitecontroller.client.NotificationClient;
import com.websitecontroller.client.PostClient;
import com.websitecontroller.dto.NotificationDTO;
import com.websitecontroller.dto.PostResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostClient postClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AuthorController authorController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authorController).build();
    }

    @Test
    void testGetDashboardData_Success() throws Exception {
        int authorId = 101;

        // Mock Post Data
        PostResponseDTO post = new PostResponseDTO();
        post.setPostId(1);
        post.setTitle("Author's First Post");
        
        // Mock Notification Data
        NotificationDTO note = new NotificationDTO();
        note.setId(500L);
        note.setMessage("Someone liked your post");

        // Define Behavior
        when(postClient.getPostsByAuthor(authorId)).thenReturn(List.of(post));
        when(notificationClient.getNotificationsForUser(authorId)).thenReturn(List.of(note));

        // Execute and Verify
        mockMvc.perform(get("/api/author/dashboard/{authorId}", authorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myPosts", hasSize(1)))
                .andExpect(jsonPath("$.myPosts[0].title", is("Author's First Post")))
                .andExpect(jsonPath("$.notifications", hasSize(1)))
                .andExpect(jsonPath("$.notifications[0].message", is("Someone liked your post")));
    }
}