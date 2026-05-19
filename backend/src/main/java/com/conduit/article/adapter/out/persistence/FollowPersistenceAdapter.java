package com.conduit.article.adapter.out.persistence;

import com.conduit.article.domain.port.out.FollowRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class FollowPersistenceAdapter implements FollowRepository {

  private final FollowJpaRepository followJpaRepository;

  public FollowPersistenceAdapter(FollowJpaRepository followJpaRepository) {
    this.followJpaRepository = followJpaRepository;
  }

  @Override
  public boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId) {
    return followJpaRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
  }

  @Override
  public Set<Long> findFollowedUserIds(Long followerId, List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Set.of();
    }
    return new HashSet<>(
        followJpaRepository.findFolloweeIdsByFollowerIdAndFolloweeIdIn(followerId, userIds));
  }

  @Override
  public List<Long> findFolloweeIds(Long followerId) {
    return followJpaRepository.findFolloweeIdsByFollowerId(followerId);
  }
}
