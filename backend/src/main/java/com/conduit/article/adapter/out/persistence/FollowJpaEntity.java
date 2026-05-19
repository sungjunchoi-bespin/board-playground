package com.conduit.article.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "follows")
@IdClass(FollowId.class)
public class FollowJpaEntity {

  @Id
  @Column(name = "follower_id")
  private Long followerId;

  @Id
  @Column(name = "followee_id")
  private Long followeeId;

  @Column(name = "created_at")
  private Instant createdAt;

  protected FollowJpaEntity() {}

  public FollowJpaEntity(Long followerId, Long followeeId) {
    this.followerId = followerId;
    this.followeeId = followeeId;
    this.createdAt = Instant.now();
  }

  public Long getFollowerId() {
    return followerId;
  }

  public Long getFolloweeId() {
    return followeeId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
