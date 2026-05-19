package com.conduit.profile;

import com.conduit.user.adapter.out.persistence.UserJpaEntity;
import com.conduit.user.adapter.out.persistence.UserJpaRepository;
import com.conduit.shared.security.JwtTokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;

    private UserJpaEntity user1;
    private UserJpaEntity user2;
    private String token1;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(
                new UserJpaEntity("user1@test.com", "user1", passwordEncoder.encode("pass"), null, null));
        token1 = jwtTokenProvider.generateToken(user1.getId());

        user2 = userRepository.save(
                new UserJpaEntity("user2@test.com", "user2", passwordEncoder.encode("pass"), null, null));
    }

    @Test
    void getProfile_unauthenticated_returnsFollowingFalse() throws Exception {
        mockMvc.perform(get("/api/profiles/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("user2"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }

    @Test
    void getProfile_authenticated_returnsFollowingFalse() throws Exception {
        mockMvc.perform(get("/api/profiles/user2")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("user2"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }

    @Test
    void getProfile_notFound_returns500() throws Exception {
        mockMvc.perform(get("/api/profiles/nonexistent"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void follow_thenGetProfile_returnsFollowingTrue() throws Exception {
        mockMvc.perform(post("/api/profiles/user2/follow")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("user2"))
                .andExpect(jsonPath("$.profile.following").value(true));

        // Verify via GET
        mockMvc.perform(get("/api/profiles/user2")
                        .header("Authorization", "Token " + token1))
                .andExpect(jsonPath("$.profile.following").value(true));
    }

    @Test
    void follow_idempotent() throws Exception {
        mockMvc.perform(post("/api/profiles/user2/follow")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().isOk());

        // Follow again — should still succeed
        mockMvc.perform(post("/api/profiles/user2/follow")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.following").value(true));
    }

    @Test
    void unfollow_afterFollow_returnsFollowingFalse() throws Exception {
        // Follow first
        mockMvc.perform(post("/api/profiles/user2/follow")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().isOk());

        // Unfollow
        mockMvc.perform(delete("/api/profiles/user2/follow")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("user2"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }

    @Test
    void follow_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/profiles/user2/follow"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void follow_nonexistentUser_returns500() throws Exception {
        mockMvc.perform(post("/api/profiles/ghost/follow")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void follow_self_returns500() throws Exception {
        mockMvc.perform(post("/api/profiles/user1/follow")
                        .header("Authorization", "Token " + token1))
                .andExpect(status().is5xxServerError());
    }
}
