package com.conduit.profile.application.service;

import com.conduit.profile.domain.model.Profile;
import com.conduit.profile.domain.port.in.FollowUserUseCase;
import com.conduit.profile.domain.port.in.GetProfileUseCase;
import com.conduit.profile.domain.port.in.UnfollowUserUseCase;
import com.conduit.profile.domain.port.out.ProfileFollowRepository;
import com.conduit.profile.domain.port.out.ProfileUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService implements GetProfileUseCase, FollowUserUseCase, UnfollowUserUseCase {

    private final ProfileUserRepository userRepository;
    private final ProfileFollowRepository followRepository;

    public ProfileService(ProfileUserRepository userRepository,
                          ProfileFollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    @Override
    public Profile getProfile(String username, Long currentUserId) {
        return userRepository.findByUsername(username, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Override
    @Transactional
    public Profile follow(String username, Long currentUserId) {
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        Long targetUserId = userRepository.findUserIdByUsername(username);

        if (targetUserId.equals(currentUserId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        if (!followRepository.isFollowing(currentUserId, targetUserId)) {
            followRepository.follow(currentUserId, targetUserId);
        }

        return new Profile(username,
                userRepository.findByUsername(username, currentUserId).orElseThrow().bio(),
                userRepository.findByUsername(username, currentUserId).orElseThrow().image(),
                true);
    }

    @Override
    @Transactional
    public Profile unfollow(String username, Long currentUserId) {
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        Long targetUserId = userRepository.findUserIdByUsername(username);

        followRepository.unfollow(currentUserId, targetUserId);

        return new Profile(username,
                userRepository.findByUsername(username, currentUserId).orElseThrow().bio(),
                userRepository.findByUsername(username, currentUserId).orElseThrow().image(),
                false);
    }
}
