package com.conduit.article.domain.port.in;

import com.conduit.article.domain.model.Article;

public interface GetArticleUseCase {

  Article getBySlug(String slug);
}
