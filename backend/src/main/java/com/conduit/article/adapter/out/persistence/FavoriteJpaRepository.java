package com.conduit.article.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteJpaRepository extends JpaRepository<FavoriteJpaEntity, FavoriteId> {

  boolean existsByUserIdAndArticleId(Long userId, Long articleId);

  @Query("SELECT f.articleId FROM FavoriteJpaEntity f WHERE f.userId = :userId AND f.articleId IN :articleIds")
  List<Long> findArticleIdsByUserIdAndArticleIdIn(
      @Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}
