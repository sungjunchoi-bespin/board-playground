package com.conduit.comment.application.service;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.out.ArticleRepository;
import com.conduit.comment.domain.model.Comment;
import com.conduit.comment.domain.port.out.CommentRepository;
import com.conduit.shared.exception.ApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    private CommentRepository commentRepository;
    private ArticleRepository articleRepository;
    private CommentService service;

    private final Article article = new Article(
            1L, "test-slug", "Test", "desc", "body", 10L, List.of(), 0, Instant.now(), Instant.now());

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        articleRepository = mock(ArticleRepository.class);
        service = new CommentService(commentRepository, articleRepository);
    }

    @Test
    void addComment_success() {
        when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
        var saved = new Comment(1L, "Great article!", 1L, 5L, Instant.now(), Instant.now());
        when(commentRepository.save(any())).thenReturn(saved);

        Comment result = service.addComment("test-slug", "Great article!", 5L);

        assertThat(result.body()).isEqualTo("Great article!");
        verify(commentRepository).save(any());
    }

    @Test
    void addComment_articleNotFound_throws() {
        when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment("missing", "text", 1L))
                .isInstanceOf(ApiException.NotFoundException.class);
    }

    @Test
    void listComments_success() {
        when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
        var c1 = new Comment(1L, "c1", 1L, 5L, Instant.now(), Instant.now());
        var c2 = new Comment(2L, "c2", 1L, 6L, Instant.now(), Instant.now());
        when(commentRepository.findByArticleId(1L)).thenReturn(List.of(c1, c2));

        List<Comment> result = service.listComments("test-slug");

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteComment_byAuthor_success() {
        when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
        var comment = new Comment(1L, "text", 1L, 5L, Instant.now(), Instant.now());
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        service.deleteComment("test-slug", 1L, 5L);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_notAuthor_throws403() {
        when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
        var comment = new Comment(1L, "text", 1L, 5L, Instant.now(), Instant.now());
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.deleteComment("test-slug", 1L, 99L))
                .isInstanceOf(ApiException.ForbiddenException.class);
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void deleteComment_commentNotFound_throws() {
        when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteComment("test-slug", 999L, 5L))
                .isInstanceOf(ApiException.NotFoundException.class);
    }
}
