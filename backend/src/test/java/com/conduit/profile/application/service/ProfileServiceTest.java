package com.conduit.profile.application.service;

import com.conduit.profile.domain.model.Profile;
import com.conduit.profile.domain.port.out.ProfileFollowRepository;
import com.conduit.profile.domain.port.out.ProfileUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private ProfileUserRepository userRepository;
    private ProfileFollowRepository followRepository;
    private ProfileService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(ProfileUserRepository.class);
        followRepository = mock(ProfileFollowRepository.class);
        service = new ProfileService(userRepository, followRepository);
    }

    @Test
    void getProfile_returnsProfile() {
        Profile expected = new Profile("jake", "bio", "img.png", false);
        when(userRepository.findByUsername("jake", 1L)).thenReturn(Optional.of(expected));

        Profile result = service.getProfile("jake", 1L);

        assertThat(result.username()).isEqualTo("jake");
        assertThat(result.following()).isFalse();
    }

    @Test
    void getProfile_userNotFound_throws() {
        when(userRepository.findByUsername("ghost", null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile("ghost", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void follow_createsFollow_returnsFollowingTrue() {
        when(userRepository.existsByUsername("jake")).thenReturn(true);
        when(userRepository.findUserIdByUsername("jake")).thenReturn(2L);
        when(followRepository.isFollowing(1L, 2L)).thenReturn(false);
        when(userRepository.findByUsername("jake", 1L))
                .thenReturn(Optional.of(new Profile("jake", "bio", "img.png", true)));

        Profile result = service.follow("jake", 1L);

        assertThat(result.following()).isTrue();
        verify(followRepository).follow(1L, 2L);
    }

    @Test
    void follow_alreadyFollowing_idempotent() {
        when(userRepository.existsByUsername("jake")).thenReturn(true);
        when(userRepository.findUserIdByUsername("jake")).thenReturn(2L);
        when(followRepository.isFollowing(1L, 2L)).thenReturn(true);
        when(userRepository.findByUsername("jake", 1L))
                .thenReturn(Optional.of(new Profile("jake", "bio", "img.png", true)));

        Profile result = service.follow("jake", 1L);

        assertThat(result.following()).isTrue();
        verify(followRepository, never()).follow(anyLong(), anyLong());
    }

    @Test
    void follow_self_throws() {
        when(userRepository.existsByUsername("me")).thenReturn(true);
        when(userRepository.findUserIdByUsername("me")).thenReturn(1L);

        assertThatThrownBy(() -> service.follow("me", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot follow yourself");
    }

    @Test
    void unfollow_removesFollow_returnsFollowingFalse() {
        when(userRepository.existsByUsername("jake")).thenReturn(true);
        when(userRepository.findUserIdByUsername("jake")).thenReturn(2L);
        when(userRepository.findByUsername("jake", 1L))
                .thenReturn(Optional.of(new Profile("jake", "bio", "img.png", false)));

        Profile result = service.unfollow("jake", 1L);

        assertThat(result.following()).isFalse();
        verify(followRepository).unfollow(1L, 2L);
    }

    @Test
    void unfollow_userNotFound_throws() {
        when(userRepository.existsByUsername("ghost")).thenReturn(false);

        assertThatThrownBy(() -> service.unfollow("ghost", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
