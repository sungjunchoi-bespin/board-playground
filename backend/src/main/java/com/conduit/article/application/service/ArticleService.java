package com.conduit.article.application.service;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.in.CreateArticleUseCase;
import com.conduit.article.domain.port.in.DeleteArticleUseCase;
import com.conduit.article.domain.port.in.GetArticleUseCase;
import com.conduit.article.domain.port.in.UpdateArticleUseCase;
import com.conduit.article.domain.port.out.ArticleRepository;
import com.conduit.shared.exception.ApiException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArticleService
    implements CreateArticleUseCase, GetArticleUseCase, UpdateArticleUseCase, DeleteArticleUseCase {

  private final ArticleRepository articleRepository;

  public ArticleService(ArticleRepository articleRepository) {
    this.articleRepository = articleRepository;
  }

  @Override
  public Article create(
      String title, String description, String body, Long authorId, List<String> tagList) {
    Article article = Article.create(title, description, body, authorId, tagList);

    String baseSlug = article.getSlug();
    String slug = baseSlug;
    int suffix = 1;
    while (articleRepository.existsBySlug(slug)) {
      slug = baseSlug + "-" + suffix;
      suffix++;
    }
    article.setSlug(slug);

    return articleRepository.save(article);
  }

  @Override
  @Transactional(readOnly = true)
  public Article getBySlug(String slug) {
    return articleRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ApiException.NotFoundException("article not found"));
  }

  @Override
  public Article update(String slug, Long userId, String title, String description, String body) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ApiException.NotFoundException("article not found"));

    if (!article.getAuthorId().equals(userId)) {
      throw new ApiException.ForbiddenException("you are not the author of this article");
    }

    article.update(title, description, body);

    if (title != null) {
      String baseSlug = article.getSlug();
      String newSlug = baseSlug;
      int suffix = 1;
      while (articleRepository.existsBySlug(newSlug)) {
        newSlug = baseSlug + "-" + suffix;
        suffix++;
      }
      article.setSlug(newSlug);
    }

    return articleRepository.save(article);
  }

  @Override
  public void delete(String slug, Long userId) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ApiException.NotFoundException("article not found"));

    if (!article.getAuthorId().equals(userId)) {
      throw new ApiException.ForbiddenException("you are not the author of this article");
    }

    articleRepository.delete(article);
  }
}
