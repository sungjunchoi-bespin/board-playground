package com.conduit.article.domain.port.out;

import com.conduit.article.domain.model.Article;
import java.util.Optional;

public interface ArticleRepository {

  Article save(Article article);

  Optional<Article> findBySlug(String slug);

  void delete(Article article);

  boolean existsBySlug(String slug);
}
