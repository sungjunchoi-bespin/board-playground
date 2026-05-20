package com.conduit.comment.adapter.out.persistence;

import com.conduit.shared.config.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "comments")
public class CommentJpaEntity extends BaseEntity {

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;

  @Column(name = "article_id", nullable = false)
  private Long articleId;

  @Column(name = "author_id", nullable = false)
  private Long authorId;

  protected CommentJpaEntity() {}

  public CommentJpaEntity(String body, Long articleId, Long authorId) {
    this.body = body;
    this.articleId = articleId;
    this.authorId = authorId;
  }

  public String getBody() {
    return body;
  }

  public Long getArticleId() {
    return articleId;
  }

  public Long getAuthorId() {
    return authorId;
  }
}
