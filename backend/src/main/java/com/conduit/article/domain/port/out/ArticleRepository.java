package com.conduit.article.domain.port.out;

import com.conduit.article.domain.model.Article;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

  Article save(Article article);

  Optional<Article> findBySlug(String slug);

  void delete(Article article);

  boolean existsBySlug(String slug);

  List<Article> findAll(
      String tag, String authorUsername, String favoritedByUsername, int limit, int offset);

  long countAll(String tag, String authorUsername, String favoritedByUsername);

  List<Article> findByAuthorIds(List<Long> authorIds, int limit, int offset);

  long countByAuthorIds(List<Long> authorIds);
}
