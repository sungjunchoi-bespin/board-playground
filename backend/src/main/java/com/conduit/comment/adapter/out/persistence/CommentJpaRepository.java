package com.conduit.comment.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {

  List<CommentJpaEntity> findByArticleIdOrderByCreatedAtDesc(Long articleId);
}
