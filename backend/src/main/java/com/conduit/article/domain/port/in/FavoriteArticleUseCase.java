package com.conduit.article.domain.port.in;

import com.conduit.article.domain.model.Article;

public interface FavoriteArticleUseCase {

    Article favorite(String slug, Long userId);
}
