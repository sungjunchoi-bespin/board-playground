package com.conduit.article.domain.port.in;

import com.conduit.article.domain.model.Article;

public interface UnfavoriteArticleUseCase {

    Article unfavorite(String slug, Long userId);
}
