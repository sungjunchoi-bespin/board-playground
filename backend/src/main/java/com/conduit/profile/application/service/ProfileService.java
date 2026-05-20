package com.conduit.profile.application.service;

import com.conduit.profile.domain.model.Profile;
import com.conduit.profile.domain.port.in.FollowUserUseCase;
import com.conduit.profile.domain.port.in.GetProfileUseCase;
import com.conduit.profile.domain.port.in.UnfollowUserUseCase;
import com.conduit.profile.domain.port.out.ProfileFollowRepository;
import com.conduit.profile.domain.port.out.ProfileUserRepository;
import com.conduit.shared.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService implements GetProfileUseCase, FollowUserUseCase, UnfollowUserUseCase {

  private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

  private final ProfileUserRepository userRepository;
  private final ProfileFollowRepository followRepository;

  public ProfileService(
      ProfileUserRepository userRepository, ProfileFollowRepository followRepository) {
    this.userRepository = userRepository;
    this.followRepository = followRepository;
  }

  @Override
  public Profile getProfile(String username, Long currentUserId) {
    return userRepository
        .findByUsername(username, currentUserId)
        .orElseThrow(() -> new ApiException.NotFoundException("user not found"));
  }

  @Override
  @Transactional
  public Profile follow(String username, Long currentUserId) {
    if (!userRepository.existsByUsername(username)) {
      throw new ApiException.NotFoundException("user not found");
    }

    Long targetUserId = userRepository.findUserIdByUsername(username);

    if (targetUserId.equals(currentUserId)) {
      throw new ApiException.ValidationException("cannot follow yourself");
    }

    if (!followRepository.isFollowing(currentUserId, targetUserId)) {
      followRepository.follow(currentUserId, targetUserId);
      log.info("User followed: followerId={}, followee={}", currentUserId, username);
    }

    return new Profile(
        username,
        userRepository.findByUsername(username, currentUserId).orElseThrow().bio(),
        userRepository.findByUsername(username, currentUserId).orElseThrow().image(),
        true);
  }

  @Override
  @Transactional
  public Profile unfollow(String username, Long currentUserId) {
    if (!userRepository.existsByUsername(username)) {
      throw new ApiException.NotFoundException("user not found");
    }

    Long targetUserId = userRepository.findUserIdByUsername(username);

    followRepository.unfollow(currentUserId, targetUserId);
    log.info("User unfollowed: followerId={}, followee={}", currentUserId, username);

    return new Profile(
        username,
        userRepository.findByUsername(username, currentUserId).orElseThrow().bio(),
        userRepository.findByUsername(username, currentUserId).orElseThrow().image(),
        false);
  }
}
