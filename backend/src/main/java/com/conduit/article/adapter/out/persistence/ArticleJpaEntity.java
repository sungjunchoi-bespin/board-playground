package com.conduit.article.adapter.out.persistence;

import com.conduit.shared.config.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
public class ArticleJpaEntity extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 512)
  private String description;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;

  @Column(name = "author_id", nullable = false)
  private Long authorId;

  @Column(name = "favorites_count", nullable = false)
  private int favoritesCount;

  @ManyToMany
  @JoinTable(
      name = "article_tags",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<TagJpaEntity> tags = new ArrayList<>();

  protected ArticleJpaEntity() {}

  public ArticleJpaEntity(
      String slug,
      String title,
      String description,
      String body,
      Long authorId,
      int favoritesCount) {
    this.slug = slug;
    this.title = title;
    this.description = description;
    this.body = body;
    this.authorId = authorId;
    this.favoritesCount = favoritesCount;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  public int getFavoritesCount() {
    return favoritesCount;
  }

  public void setFavoritesCount(int favoritesCount) {
    this.favoritesCount = favoritesCount;
  }

  public List<TagJpaEntity> getTags() {
    return tags;
  }

  public void setTags(List<TagJpaEntity> tags) {
    this.tags = tags;
  }
}
