package com.conduit.article.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArticleTest {

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("should create article with generated slug")
    void success() {
      Article article =
          Article.create("How to train your dragon", "Ever wonder how?", "It takes a village...",
              1L, List.of("dragons", "training"));

      assertThat(article.getId()).isNull();
      assertThat(article.getSlug()).isEqualTo("how-to-train-your-dragon");
      assertThat(article.getTitle()).isEqualTo("How to train your dragon");
      assertThat(article.getDescription()).isEqualTo("Ever wonder how?");
      assertThat(article.getBody()).isEqualTo("It takes a village...");
      assertThat(article.getAuthorId()).isEqualTo(1L);
      assertThat(article.getTagList()).containsExactly("dragons", "training");
      assertThat(article.getFavoritesCount()).isZero();
    }

    @Test
    @DisplayName("should create article with empty tag list when null")
    void nullTagList() {
      Article article = Article.create("Title", "Desc", "Body", 1L, null);

      assertThat(article.getTagList()).isEmpty();
    }
  }

  @Nested
  @DisplayName("generateSlug")
  class GenerateSlug {

    @Test
    @DisplayName("should lowercase and replace non-alphanumeric with hyphens")
    void basicSlug() {
      assertThat(Article.generateSlug("How to Train Your Dragon"))
          .isEqualTo("how-to-train-your-dragon");
    }

    @Test
    @DisplayName("should trim leading and trailing hyphens")
    void trimHyphens() {
      assertThat(Article.generateSlug("  --Hello World--  ")).isEqualTo("hello-world");
    }

    @Test
    @DisplayName("should handle special characters")
    void specialChars() {
      assertThat(Article.generateSlug("What's up? 100% awesome!"))
          .isEqualTo("what-s-up-100-awesome");
    }

    @Test
    @DisplayName("should truncate to max 100 chars")
    void maxLength() {
      String longTitle = "a".repeat(200);
      String slug = Article.generateSlug(longTitle);
      assertThat(slug.length()).isLessThanOrEqualTo(100);
    }

    @Test
    @DisplayName("should return empty for blank title")
    void blankTitle() {
      assertThat(Article.generateSlug("")).isEmpty();
      assertThat(Article.generateSlug("   ")).isEmpty();
    }

    @Test
    @DisplayName("should return empty for null title")
    void nullTitle() {
      assertThat(Article.generateSlug(null)).isEmpty();
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("should update title and regenerate slug")
    void updateTitle() {
      Article article =
          Article.create("Old Title", "Desc", "Body", 1L, List.of());

      article.update("New Title", null, null);

      assertThat(article.getTitle()).isEqualTo("New Title");
      assertThat(article.getSlug()).isEqualTo("new-title");
      assertThat(article.getDescription()).isEqualTo("Desc");
      assertThat(article.getBody()).isEqualTo("Body");
    }

    @Test
    @DisplayName("should update description only")
    void updateDescription() {
      Article article =
          Article.create("Title", "Old Desc", "Body", 1L, List.of());

      article.update(null, "New Desc", null);

      assertThat(article.getTitle()).isEqualTo("Title");
      assertThat(article.getSlug()).isEqualTo("title");
      assertThat(article.getDescription()).isEqualTo("New Desc");
    }

    @Test
    @DisplayName("should update body only")
    void updateBody() {
      Article article =
          Article.create("Title", "Desc", "Old Body", 1L, List.of());

      article.update(null, null, "New Body");

      assertThat(article.getBody()).isEqualTo("New Body");
    }

    @Test
    @DisplayName("should update all fields")
    void updateAll() {
      Article article =
          Article.create("Old Title", "Old Desc", "Old Body", 1L, List.of());

      article.update("New Title", "New Desc", "New Body");

      assertThat(article.getTitle()).isEqualTo("New Title");
      assertThat(article.getSlug()).isEqualTo("new-title");
      assertThat(article.getDescription()).isEqualTo("New Desc");
      assertThat(article.getBody()).isEqualTo("New Body");
    }
  }
}
