package com.conduit.article.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;

public class FollowId implements Serializable {

  private Long followerId;
  private Long followeeId;

  public FollowId() {}

  public FollowId(Long followerId, Long followeeId) {
    this.followerId = followerId;
    this.followeeId = followeeId;
  }

  public Long getFollowerId() {
    return followerId;
  }

  public Long getFolloweeId() {
    return followeeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FollowId that = (FollowId) o;
    return Objects.equals(followerId, that.followerId)
        && Objects.equals(followeeId, that.followeeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(followerId, followeeId);
  }
}
