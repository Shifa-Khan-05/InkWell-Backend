package com.commentservice;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.commentservice.client.AuthClient;
import com.commentservice.client.NotificationClient; // ✅ Added Import
import com.commentservice.dto.UserResponseDTO;
import com.commentservice.entity.Comment;
import com.commentservice.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
class CommentServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @MockBean
    private AuthClient authClient;

    @MockBean
    private NotificationClient notificationClient; // ✅ Mocked the Notification Client

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        
        // Default Mock Behavior for Auth Service
        UserResponseDTO mockUser = new UserResponseDTO();
        mockUser.setFullName("Shifa Khan");
        when(authClient.getUserById(anyInt())).thenReturn(mockUser);

        // ✅ Mock Notification Service to do nothing when called
        try {
            doNothing().when(notificationClient).sendNotification(anyMap());
        } catch (Exception e) {
            // Silence exception for mocking
        }
    }

    @Test
    void shouldAddCommentAsPending() throws Exception {
        Comment comment = new Comment();
        comment.setContent("Great post!");
        comment.setPostId(101);
        comment.setUserId(1);
        comment.setAuthorId(14); // Added authorId to match entity

        mockMvc.perform(post("/comments/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.content").value("Great post!"));
    }

    @Test
    void shouldApproveComment() throws Exception {
        // Updated constructor to match your @AllArgsConstructor in Entity
        Comment comment = new Comment(null, 101, 1, 14, "Verify me", 0L, "PENDING", 0, null);
        Comment saved = commentRepository.save(comment);
        
        mockMvc.perform(put("/comments/" + saved.getCommentId() + "/approve"))
                .andExpect(status().isOk());

        Comment updated = commentRepository.findById(saved.getCommentId()).get();
        assertEquals("APPROVED", updated.getStatus());
    }

    @Test
    void shouldOnlyReturnApprovedCommentsForPost() throws Exception {
        Comment c1 = new Comment(null, 101, 1, 14, "Approved Comment", 0L, "APPROVED", 0, null);
        Comment c2 = new Comment(null, 101, 1, 14, "Pending Comment", 0L, "PENDING", 0, null);
        commentRepository.saveAll(List.of(c1, c2));

        mockMvc.perform(get("/comments/post/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("Approved Comment"))
                .andExpect(jsonPath("$[0].authorName").value("Shifa Khan"));
    }

    @Test
    void shouldIncreaseLikeCount() throws Exception {
        Comment saved = commentRepository.save(new Comment(null, 101, 1, 14, "Like me", 0L, "APPROVED", 5, null));

        mockMvc.perform(post("/comments/" + saved.getCommentId() + "/like"))
                .andExpect(status().isOk());

        Comment updated = commentRepository.findById(saved.getCommentId()).get();
        assertEquals(6, updated.getLikesCount());
    }
    
    // Helper for assertions
    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but found " + actual);
        }
    }
}
