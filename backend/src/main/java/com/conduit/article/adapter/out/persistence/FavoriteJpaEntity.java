package com.conduit.article.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
public class FavoriteJpaEntity {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @Id
  @Column(name = "article_id")
  private Long articleId;

  @Column(name = "created_at")
  private Instant createdAt;

  protected FavoriteJpaEntity() {}

  public FavoriteJpaEntity(Long userId, Long articleId) {
    this.userId = userId;
    this.articleId = articleId;
    this.createdAt = Instant.now();
  }

  public Long getUserId() {
    return userId;
  }

  public Long getArticleId() {
    return articleId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
