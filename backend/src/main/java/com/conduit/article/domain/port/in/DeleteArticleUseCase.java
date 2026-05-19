package com.conduit.article.domain.port.in;

public interface DeleteArticleUseCase {

  void delete(String slug, Long userId);
}
