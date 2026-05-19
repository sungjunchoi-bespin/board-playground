package com.conduit.comment.adapter.out.persistence;

import com.conduit.comment.domain.model.Comment;
import com.conduit.comment.domain.port.out.CommentRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentPersistenceAdapter implements CommentRepository {

    private final CommentJpaRepository jpaRepository;

    public CommentPersistenceAdapter(CommentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Comment save(Comment comment) {
        var entity = new CommentJpaEntity(comment.body(), comment.articleId(), comment.authorId());
        var saved = jpaRepository.save(entity);
        return toComment(saved);
    }

    @Override
    public List<Comment> findByArticleId(Long articleId) {
        return jpaRepository.findByArticleIdOrderByCreatedAtDesc(articleId)
                .stream()
                .map(this::toComment)
                .toList();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return jpaRepository.findById(id).map(this::toComment);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Comment toComment(CommentJpaEntity entity) {
        return new Comment(
                entity.getId(),
                entity.getBody(),
                entity.getArticleId(),
                entity.getAuthorId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
