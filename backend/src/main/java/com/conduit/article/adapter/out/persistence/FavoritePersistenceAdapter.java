package com.conduit.article.adapter.out.persistence;

import com.conduit.article.domain.port.out.FavoriteRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class FavoritePersistenceAdapter implements FavoriteRepository {

  private final FavoriteJpaRepository favoriteJpaRepository;

  public FavoritePersistenceAdapter(FavoriteJpaRepository favoriteJpaRepository) {
    this.favoriteJpaRepository = favoriteJpaRepository;
  }

  @Override
  public boolean existsByUserIdAndArticleId(Long userId, Long articleId) {
    return favoriteJpaRepository.existsByUserIdAndArticleId(userId, articleId);
  }

  @Override
  public Set<Long> findFavoritedArticleIds(Long userId, List<Long> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) {
      return Set.of();
    }
    return new HashSet<>(
        favoriteJpaRepository.findArticleIdsByUserIdAndArticleIdIn(userId, articleIds));
  }

  @Override
  public void save(Long userId, Long articleId) {
    favoriteJpaRepository.save(new FavoriteJpaEntity(userId, articleId));
  }

  @Override
  public void delete(Long userId, Long articleId) {
    FavoriteId id = new FavoriteId(userId, articleId);
    if (favoriteJpaRepository.existsById(id)) {
      favoriteJpaRepository.deleteById(id);
    }
  }

  @Override
  public int countByArticleId(Long articleId) {
    return favoriteJpaRepository.countByArticleId(articleId);
  }
}
