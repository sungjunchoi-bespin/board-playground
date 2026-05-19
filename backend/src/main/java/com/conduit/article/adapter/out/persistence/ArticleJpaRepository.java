package com.conduit.article.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleJpaRepository extends JpaRepository<ArticleJpaEntity, Long> {

  Optional<ArticleJpaEntity> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
