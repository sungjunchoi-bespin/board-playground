package com.conduit.article.domain.port.in;

import com.conduit.article.domain.model.Article;
import java.util.List;

public interface ListArticlesUseCase {

  List<Article> listArticles(String tag, String author, String favorited, int limit, int offset);

  long countArticles(String tag, String author, String favorited);
}
