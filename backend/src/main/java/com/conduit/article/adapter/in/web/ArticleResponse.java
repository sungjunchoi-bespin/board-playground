package com.conduit.article.adapter.in.web;

import com.conduit.article.domain.model.Article;
import com.conduit.user.domain.model.User;
import java.time.Instant;
import java.util.List;

public record ArticleResponse(
    String slug,
    String title,
    String description,
    String body,
    List<String> tagList,
    Instant createdAt,
    Instant updatedAt,
    boolean favorited,
    int favoritesCount,
    AuthorResponse author) {

  public static ArticleResponse from(Article article, User author) {
    return from(article, author, false, false);
  }

  public static ArticleResponse from(
      Article article, User author, boolean favorited, boolean following) {
    return new ArticleResponse(
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        article.getTagList(),
        article.getCreatedAt(),
        article.getUpdatedAt(),
        favorited,
        article.getFavoritesCount(),
        new AuthorResponse(author.getUsername(), author.getBio(), author.getImage(), following));
  }

  public record AuthorResponse(String username, String bio, String image, boolean following) {}
}
