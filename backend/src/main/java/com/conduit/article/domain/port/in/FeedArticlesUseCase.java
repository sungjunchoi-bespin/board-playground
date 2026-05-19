package com.conduit.article.domain.port.in;

import com.conduit.article.domain.model.Article;
import java.util.List;

public interface FeedArticlesUseCase {

  List<Article> feedArticles(Long userId, int limit, int offset);

  long countFeedArticles(Long userId);
}
