package com.conduit.comment;

import com.conduit.article.adapter.out.persistence.ArticleJpaEntity;
import com.conduit.article.adapter.out.persistence.ArticleJpaRepository;
import com.conduit.shared.security.JwtTokenProvider;
import com.conduit.user.adapter.out.persistence.UserJpaEntity;
import com.conduit.user.adapter.out.persistence.UserJpaRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private ArticleJpaRepository articleRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private String token1;
    private String token2;
    private String articleSlug;

    @BeforeEach
    void setUp() {
        var user1 = userRepository.save(
                new UserJpaEntity("author@test.com", "author", passwordEncoder.encode("pass"), null, null));
        token1 = jwtTokenProvider.generateToken(user1.getId());

        var user2 = userRepository.save(
                new UserJpaEntity("other@test.com", "other", passwordEncoder.encode("pass"), null, null));
        token2 = jwtTokenProvider.generateToken(user2.getId());

        var article = articleRepository.save(
                new ArticleJpaEntity("test-article", "Test Article", "desc", "body", user1.getId(), 0));
        articleSlug = article.getSlug();
    }

    @Test
    void addComment_returns200() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("comment", Map.of("body", "Nice article!")));

        mockMvc.perform(post("/api/articles/{slug}/comments", articleSlug)
                        .header("Authorization", "Token " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment.body").value("Nice article!"))
                .andExpect(jsonPath("$.comment.id").isNumber())
                .andExpect(jsonPath("$.comment.author.username").value("author"));
    }

    @Test
    void addComment_unauthenticated_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("comment", Map.of("body", "text")));

        mockMvc.perform(post("/api/articles/{slug}/comments", articleSlug)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listComments_returnsEmptyInitially() throws Exception {
        mockMvc.perform(get("/api/articles/{slug}/comments", articleSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(0));
    }

    @Test
    void listComments_afterAdding_returnsComments() throws Exception {
        // Add a comment
        String body = objectMapper.writeValueAsString(
                Map.of("comment", Map.of("body", "Comment 1")));
        mockMvc.perform(post("/api/articles/{slug}/comments", articleSlug)
                        .header("Authorization", "Token " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // List
        mockMvc.perform(get("/api/articles/{slug}/comments", articleSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].body").value("Comment 1"))
                .andExpect(jsonPath("$.comments[0].author.username").value("author"));
    }

    @Test
    void deleteComment_byAuthor_returns200() throws Exception {
        // Add comment
        String body = objectMapper.writeValueAsString(
                Map.of("comment", Map.of("body", "To delete")));
        MvcResult result = mockMvc.perform(post("/api/articles/{slug}/comments", articleSlug)
                        .header("Authorization", "Token " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        Integer commentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("comment").path("id").asInt();

        // Delete
        mockMvc.perform(delete("/api/articles/{slug}/comments/{id}", articleSlug, commentId)
                        .header("Authorization", "Token " + token1))
                .andExpect(status().isOk());

        // Verify gone
        mockMvc.perform(get("/api/articles/{slug}/comments", articleSlug))
                .andExpect(jsonPath("$.comments.length()").value(0));
    }

    @Test
    void deleteComment_byNonAuthor_returns403() throws Exception {
        // Add comment as user1
        String body = objectMapper.writeValueAsString(
                Map.of("comment", Map.of("body", "Protected")));
        MvcResult result = mockMvc.perform(post("/api/articles/{slug}/comments", articleSlug)
                        .header("Authorization", "Token " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        Integer commentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("comment").path("id").asInt();

        // Try delete as user2
        mockMvc.perform(delete("/api/articles/{slug}/comments/{id}", articleSlug, commentId)
                        .header("Authorization", "Token " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/articles/{slug}/comments/1", articleSlug))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addComment_articleNotFound_returns404() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("comment", Map.of("body", "text")));

        mockMvc.perform(post("/api/articles/{slug}/comments", "nonexistent")
                        .header("Authorization", "Token " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
