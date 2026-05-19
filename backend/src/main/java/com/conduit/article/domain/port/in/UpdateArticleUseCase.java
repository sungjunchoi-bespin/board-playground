package com.conduit.article.domain.port.in;

import com.conduit.article.domain.model.Article;

public interface UpdateArticleUseCase {

  Article update(String slug, Long userId, String title, String description, String body);
}
