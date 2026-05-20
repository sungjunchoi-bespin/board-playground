package com.conduit.article.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowJpaRepository extends JpaRepository<FollowJpaEntity, FollowId> {

  boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

  @Query(
      "SELECT f.followeeId FROM FollowJpaEntity f WHERE f.followerId = :followerId AND f.followeeId IN :followeeIds")
  List<Long> findFolloweeIdsByFollowerIdAndFolloweeIdIn(
      @Param("followerId") Long followerId, @Param("followeeIds") List<Long> followeeIds);

  @Query("SELECT f.followeeId FROM FollowJpaEntity f WHERE f.followerId = :followerId")
  List<Long> findFolloweeIdsByFollowerId(@Param("followerId") Long followerId);
}
