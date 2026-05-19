package com.conduit.article.domain.port.out;

import java.util.List;
import java.util.Set;

public interface FollowRepository {

  boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

  Set<Long> findFollowedUserIds(Long followerId, List<Long> userIds);

  List<Long> findFolloweeIds(Long followerId);
}
