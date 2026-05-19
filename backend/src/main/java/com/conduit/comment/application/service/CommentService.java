package com.conduit.comment.application.service;

import com.conduit.article.domain.port.out.ArticleRepository;
import com.conduit.comment.domain.model.Comment;
import com.conduit.comment.domain.port.in.AddCommentUseCase;
import com.conduit.comment.domain.port.in.DeleteCommentUseCase;
import com.conduit.comment.domain.port.in.ListCommentsUseCase;
import com.conduit.comment.domain.port.out.CommentRepository;
import com.conduit.shared.exception.ApiException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService implements AddCommentUseCase, ListCommentsUseCase, DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    public CommentService(CommentRepository commentRepository, ArticleRepository articleRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public Comment addComment(String slug, String body, Long authorId) {
        var article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException.NotFoundException("Article not found"));

        var comment = new Comment(null, body, article.getId(), authorId, Instant.now(), Instant.now());
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> listComments(String slug) {
        var article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException.NotFoundException("Article not found"));

        return commentRepository.findByArticleId(article.getId());
    }

    @Override
    @Transactional
    public void deleteComment(String slug, Long commentId, Long currentUserId) {
        // Verify article exists
        articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException.NotFoundException("Article not found"));

        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException.NotFoundException("Comment not found"));

        if (!comment.authorId().equals(currentUserId)) {
            throw new ApiException.ForbiddenException("You cannot delete this comment");
        }

        commentRepository.deleteById(commentId);
    }
}
