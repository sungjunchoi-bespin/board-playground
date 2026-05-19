package com.conduit.article.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.out.ArticleRepository;
import com.conduit.article.domain.port.out.FollowRepository;
import com.conduit.shared.exception.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private FollowRepository followRepository;
  private ArticleService articleService;

  @BeforeEach
  void setUp() {
    articleService = new ArticleService(articleRepository, followRepository);
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("should create article with unique slug")
    void success() {
      when(articleRepository.existsBySlug("how-to-train-your-dragon")).thenReturn(false);
      when(articleRepository.save(any(Article.class)))
          .thenAnswer(
              inv -> {
                Article a = inv.getArgument(0);
                return new Article(
                    1L,
                    a.getSlug(),
                    a.getTitle(),
                    a.getDescription(),
                    a.getBody(),
                    a.getAuthorId(),
                    a.getTagList(),
                    0,
                    Instant.now(),
                    Instant.now());
              });

      Article result =
          articleService.create(
              "How to train your dragon",
              "Ever wonder how?",
              "It takes a village...",
              1L,
              List.of("dragons", "training"));

      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getSlug()).isEqualTo("how-to-train-your-dragon");
      assertThat(result.getTitle()).isEqualTo("How to train your dragon");
      assertThat(result.getTagList()).containsExactly("dragons", "training");
    }

    @Test
    @DisplayName("should append suffix when slug is duplicate")
    void duplicateSlug() {
      when(articleRepository.existsBySlug("how-to-train-your-dragon")).thenReturn(true);
      when(articleRepository.existsBySlug("how-to-train-your-dragon-1")).thenReturn(false);
      when(articleRepository.save(any(Article.class)))
          .thenAnswer(
              inv -> {
                Article a = inv.getArgument(0);
                return new Article(
                    1L,
                    a.getSlug(),
                    a.getTitle(),
                    a.getDescription(),
                    a.getBody(),
                    a.getAuthorId(),
                    a.getTagList(),
                    0,
                    Instant.now(),
                    Instant.now());
              });

      Article result =
          articleService.create(
              "How to train your dragon", "Desc", "Body", 1L, List.of());

      assertThat(result.getSlug()).isEqualTo("how-to-train-your-dragon-1");
    }
  }

  @Nested
  @DisplayName("getBySlug")
  class GetBySlug {

    @Test
    @DisplayName("should return article when found")
    void success() {
      Article article =
          new Article(
              1L,
              "test-slug",
              "Test Title",
              "Desc",
              "Body",
              1L,
              List.of(),
              0,
              Instant.now(),
              Instant.now());
      when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

      Article result = articleService.getBySlug("test-slug");

      assertThat(result.getSlug()).isEqualTo("test-slug");
      assertThat(result.getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("should throw NotFoundException when article not found")
    void notFound() {
      when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> articleService.getBySlug("nonexistent"))
          .isInstanceOf(ApiException.NotFoundException.class)
          .hasMessageContaining("article not found");
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("should update article when user is author")
    void success() {
      Article existing =
          new Article(
              1L,
              "old-title",
              "Old Title",
              "Old Desc",
              "Old Body",
              1L,
              List.of("tag1"),
              0,
              Instant.now(),
              Instant.now());
      when(articleRepository.findBySlug("old-title")).thenReturn(Optional.of(existing));
      when(articleRepository.existsBySlug("new-title")).thenReturn(false);
      when(articleRepository.save(any(Article.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      Article result =
          articleService.update("old-title", 1L, "New Title", "New Desc", "New Body");

      assertThat(result.getTitle()).isEqualTo("New Title");
      assertThat(result.getSlug()).isEqualTo("new-title");
      assertThat(result.getDescription()).isEqualTo("New Desc");
      assertThat(result.getBody()).isEqualTo("New Body");
    }

    @Test
    @DisplayName("should throw ForbiddenException when user is not author")
    void forbidden() {
      Article existing =
          new Article(
              1L,
              "test-slug",
              "Title",
              "Desc",
              "Body",
              1L,
              List.of(),
              0,
              Instant.now(),
              Instant.now());
      when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(existing));

      assertThatThrownBy(
              () -> articleService.update("test-slug", 999L, "New Title", null, null))
          .isInstanceOf(ApiException.ForbiddenException.class)
          .hasMessageContaining("you are not the author");
    }

    @Test
    @DisplayName("should throw NotFoundException when article not found")
    void notFound() {
      when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> articleService.update("nonexistent", 1L, "New Title", null, null))
          .isInstanceOf(ApiException.NotFoundException.class)
          .hasMessageContaining("article not found");
    }

    @Test
    @DisplayName("should update only description without changing slug")
    void updateDescriptionOnly() {
      Article existing =
          new Article(
              1L,
              "my-title",
              "My Title",
              "Old Desc",
              "Body",
              1L,
              List.of(),
              0,
              Instant.now(),
              Instant.now());
      when(articleRepository.findBySlug("my-title")).thenReturn(Optional.of(existing));
      when(articleRepository.save(any(Article.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      Article result = articleService.update("my-title", 1L, null, "New Desc", null);

      assertThat(result.getSlug()).isEqualTo("my-title");
      assertThat(result.getDescription()).isEqualTo("New Desc");
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("should delete article when user is author")
    void success() {
      Article existing =
          new Article(
              1L,
              "test-slug",
              "Title",
              "Desc",
              "Body",
              1L,
              List.of(),
              0,
              Instant.now(),
              Instant.now());
      when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(existing));

      articleService.delete("test-slug", 1L);

      verify(articleRepository).delete(existing);
    }

    @Test
    @DisplayName("should throw ForbiddenException when user is not author")
    void forbidden() {
      Article existing =
          new Article(
              1L,
              "test-slug",
              "Title",
              "Desc",
              "Body",
              1L,
              List.of(),
              0,
              Instant.now(),
              Instant.now());
      when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(existing));

      assertThatThrownBy(() -> articleService.delete("test-slug", 999L))
          .isInstanceOf(ApiException.ForbiddenException.class)
          .hasMessageContaining("you are not the author");

      verify(articleRepository, never()).delete(any(Article.class));
    }

    @Test
    @DisplayName("should throw NotFoundException when article not found")
    void notFound() {
      when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> articleService.delete("nonexistent", 1L))
          .isInstanceOf(ApiException.NotFoundException.class)
          .hasMessageContaining("article not found");
    }
  }
}
