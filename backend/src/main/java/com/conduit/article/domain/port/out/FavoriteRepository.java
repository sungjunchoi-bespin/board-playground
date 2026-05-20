package com.conduit.article.domain.port.out;

import java.util.List;
import java.util.Set;

public interface FavoriteRepository {

  boolean existsByUserIdAndArticleId(Long userId, Long articleId);

  Set<Long> findFavoritedArticleIds(Long userId, List<Long> articleIds);

  void save(Long userId, Long articleId);

  void delete(Long userId, Long articleId);

  int countByArticleId(Long articleId);
}
