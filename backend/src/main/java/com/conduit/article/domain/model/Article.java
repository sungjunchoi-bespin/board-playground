package com.conduit.article.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Article {

  private Long id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private Long authorId;
  private List<String> tagList;
  private int favoritesCount;
  private Instant createdAt;
  private Instant updatedAt;

  public Article(
      Long id,
      String slug,
      String title,
      String description,
      String body,
      Long authorId,
      List<String> tagList,
      int favoritesCount,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.slug = slug;
    this.title = title;
    this.description = description;
    this.body = body;
    this.authorId = authorId;
    this.tagList = tagList != null ? new ArrayList<>(tagList) : new ArrayList<>();
    this.favoritesCount = favoritesCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Article create(
      String title, String description, String body, Long authorId, List<String> tagList) {
    String slug = generateSlug(title);
    return new Article(null, slug, title, description, body, authorId, tagList, 0, null, null);
  }

  public void update(String title, String description, String body) {
    if (title != null) {
      this.title = title;
      this.slug = generateSlug(title);
    }
    if (description != null) {
      this.description = description;
    }
    if (body != null) {
      this.body = body;
    }
  }

  static String generateSlug(String title) {
    if (title == null || title.isBlank()) {
      return "";
    }
    String slug =
        title
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+", "")
            .replaceAll("-+$", "");
    if (slug.length() > 100) {
      slug = slug.substring(0, 100);
      slug = slug.replaceAll("-+$", "");
    }
    return slug;
  }

  public Long getId() {
    return id;
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

  public String getDescription() {
    return description;
  }

  public String getBody() {
    return body;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public List<String> getTagList() {
    return tagList;
  }

  public int getFavoritesCount() {
    return favoritesCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
