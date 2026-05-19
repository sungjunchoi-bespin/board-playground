package com.conduit.comment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {

    List<CommentJpaEntity> findByArticleIdOrderByCreatedAtDesc(Long articleId);
}
