package com.conduit.article.application.service;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.in.CreateArticleUseCase;
import com.conduit.article.domain.port.in.DeleteArticleUseCase;
import com.conduit.article.domain.port.in.FavoriteArticleUseCase;
import com.conduit.article.domain.port.in.FeedArticlesUseCase;
import com.conduit.article.domain.port.in.GetArticleUseCase;
import com.conduit.article.domain.port.in.ListArticlesUseCase;
import com.conduit.article.domain.port.in.UnfavoriteArticleUseCase;
import com.conduit.article.domain.port.in.UpdateArticleUseCase;
import com.conduit.article.domain.port.out.ArticleRepository;
import com.conduit.article.domain.port.out.FavoriteRepository;
import com.conduit.article.domain.port.out.FollowRepository;
import com.conduit.shared.exception.ApiException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArticleService
    implements CreateArticleUseCase,
        GetArticleUseCase,
        UpdateArticleUseCase,
        DeleteArticleUseCase,
        ListArticlesUseCase,
        FeedArticlesUseCase,
        FavoriteArticleUseCase,
        UnfavoriteArticleUseCase {

  private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

  private final ArticleRepository articleRepository;
  private final FollowRepository followRepository;
  private final FavoriteRepository favoriteRepository;

  public ArticleService(
      ArticleRepository articleRepository,
      FollowRepository followRepository,
      FavoriteRepository favoriteRepository) {
    this.articleRepository = articleRepository;
    this.followRepository = followRepository;
    this.favoriteRepository = favoriteRepository;
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

    Article saved = articleRepository.save(article);
    log.info("Article created: slug={}, authorId={}", slug, authorId);
    return saved;
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
    log.info("Article deleted: slug={}, userId={}", slug, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Article> listArticles(
      String tag, String author, String favorited, int limit, int offset) {
    return articleRepository.findAll(tag, author, favorited, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public long countArticles(String tag, String author, String favorited) {
    return articleRepository.countAll(tag, author, favorited);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Article> feedArticles(Long userId, int limit, int offset) {
    List<Long> followeeIds = followRepository.findFolloweeIds(userId);
    if (followeeIds.isEmpty()) {
      return List.of();
    }
    return articleRepository.findByAuthorIds(followeeIds, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public long countFeedArticles(Long userId) {
    List<Long> followeeIds = followRepository.findFolloweeIds(userId);
    if (followeeIds.isEmpty()) {
      return 0;
    }
    return articleRepository.countByAuthorIds(followeeIds);
  }

  @Override
  public Article favorite(String slug, Long userId) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ApiException.NotFoundException("article not found"));

    if (!favoriteRepository.existsByUserIdAndArticleId(userId, article.getId())) {
      favoriteRepository.save(userId, article.getId());
    }

    article.setFavoritesCount(favoriteRepository.countByArticleId(article.getId()));
    return articleRepository.save(article);
  }

  @Override
  public Article unfavorite(String slug, Long userId) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ApiException.NotFoundException("article not found"));

    favoriteRepository.delete(userId, article.getId());

    article.setFavoritesCount(favoriteRepository.countByArticleId(article.getId()));
    return articleRepository.save(article);
  }
}
