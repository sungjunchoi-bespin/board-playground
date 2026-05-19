package com.conduit.article.domain.port.in;

import com.conduit.article.domain.model.Article;
import java.util.List;

public interface CreateArticleUseCase {

  Article create(
      String title, String description, String body, Long authorId, List<String> tagList);
}
