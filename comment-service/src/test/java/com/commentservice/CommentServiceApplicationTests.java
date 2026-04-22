package com.commentservice;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.commentservice.client.AuthClient;
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
@AutoConfigureMockMvc
class CommentServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @MockBean
    private AuthClient authClient; // Mocking the Feign Client

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        
        // Default Mock Behavior for Auth Service
        UserResponseDTO mockUser = new UserResponseDTO();
        mockUser.setFullName("Shifa Khan");
        when(authClient.getUserById(anyInt())).thenReturn(mockUser);
    }

    @Test
    void shouldAddCommentAsPending() throws Exception {
        Comment comment = new Comment();
        comment.setContent("Great post!");
        comment.setPostId(101);
        comment.setUserId(1);

        mockMvc.perform(post("/comments/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.content").value("Great post!"));
    }

    @Test
    void shouldApproveComment() throws Exception {
        Comment saved = commentRepository.save(new Comment(null, 101, 1, "Verify me", null, "PENDING", 0, null));
        
        mockMvc.perform(put("/comments/" + saved.getCommentId() + "/approve"))
                .andExpect(status().isOk());

        Comment updated = commentRepository.findById(saved.getCommentId()).get();
        assert(updated.getStatus().equals("APPROVED"));
    }

    @Test
    void shouldOnlyReturnApprovedCommentsForPost() throws Exception {
        // One Approved, One Pending
        Comment c1 = new Comment(null, 101, 1, "Approved Comment", null, "APPROVED", 0, null);
        Comment c2 = new Comment(null, 101, 1, "Pending Comment", null, "PENDING", 0, null);
        commentRepository.saveAll(List.of(c1, c2));

        mockMvc.perform(get("/comments/post/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("Approved Comment"))
                .andExpect(jsonPath("$[0].authorName").value("Shifa Khan"));
    }

    @Test
    void shouldIncreaseLikeCount() throws Exception {
        Comment saved = commentRepository.save(new Comment(null, 101, 1, "Like me", null, "APPROVED", 5, null));

        mockMvc.perform(post("/comments/" + saved.getCommentId() + "/like"))
                .andExpect(status().isOk());

        Comment updated = commentRepository.findById(saved.getCommentId()).get();
        assert(updated.getLikesCount() == 6);
    }
}