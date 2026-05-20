package com.conduit.profile.domain.port.out;

public interface ProfileFollowRepository {

  boolean isFollowing(Long followerId, Long followeeId);

  void follow(Long followerId, Long followeeId);

  void unfollow(Long followerId, Long followeeId);
}
