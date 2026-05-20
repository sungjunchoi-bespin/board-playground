package com.conduit.article.adapter.in.web;

import com.conduit.article.domain.model.Article;
import com.conduit.user.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Article data")
public record ArticleResponse(
    @Schema(description = "URL-friendly identifier", example = "how-to-train-your-dragon")
        String slug,
    @Schema(description = "Article title", example = "How to train your dragon") String title,
    @Schema(description = "Short summary", example = "Ever wonder how?") String description,
    @Schema(description = "Article body in Markdown", example = "## Introduction\n\nContent here")
        String body,
    @Schema(description = "List of tags", example = "[\"dragons\", \"training\"]")
        List<String> tagList,
    @Schema(description = "Creation timestamp") Instant createdAt,
    @Schema(description = "Last update timestamp") Instant updatedAt,
    @Schema(description = "Whether the current user has favorited this article") boolean favorited,
    @Schema(description = "Total favorites count", example = "5") int favoritesCount,
    @Schema(description = "Author profile") AuthorResponse author) {

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

  @Schema(description = "Author profile embedded in article")
  public record AuthorResponse(
      @Schema(description = "Username", example = "jake") String username,
      @Schema(description = "Short bio", example = "I work at statefarm") String bio,
      @Schema(
              description = "Avatar URL",
              example = "https://api.realworld.io/images/smiley-cyrus.jpeg")
          String image,
      @Schema(description = "Whether the current user follows this author") boolean following) {}
}
