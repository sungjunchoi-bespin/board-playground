package com.conduit.article.adapter.out.persistence;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.out.ArticleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ArticlePersistenceAdapter implements ArticleRepository {

  private final ArticleJpaRepository articleJpaRepository;
  private final TagJpaRepository tagJpaRepository;

  public ArticlePersistenceAdapter(
      ArticleJpaRepository articleJpaRepository, TagJpaRepository tagJpaRepository) {
    this.articleJpaRepository = articleJpaRepository;
    this.tagJpaRepository = tagJpaRepository;
  }

  @Override
  public Article save(Article article) {
    ArticleJpaEntity entity;
    if (article.getId() != null) {
      entity =
          articleJpaRepository
              .findById(article.getId())
              .orElseThrow(
                  () -> new IllegalStateException("Article not found: " + article.getId()));
      entity.setSlug(article.getSlug());
      entity.setTitle(article.getTitle());
      entity.setDescription(article.getDescription());
      entity.setBody(article.getBody());
      entity.setAuthorId(article.getAuthorId());
      entity.setFavoritesCount(article.getFavoritesCount());
    } else {
      entity =
          new ArticleJpaEntity(
              article.getSlug(),
              article.getTitle(),
              article.getDescription(),
              article.getBody(),
              article.getAuthorId(),
              article.getFavoritesCount());
    }

    entity.setTags(resolveTagEntities(article.getTagList()));
    ArticleJpaEntity saved = articleJpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    return articleJpaRepository.findBySlug(slug).map(this::toDomain);
  }

  @Override
  public void delete(Article article) {
    articleJpaRepository
        .findById(article.getId())
        .ifPresent(articleJpaRepository::delete);
  }

  @Override
  public boolean existsBySlug(String slug) {
    return articleJpaRepository.existsBySlug(slug);
  }

  private List<TagJpaEntity> resolveTagEntities(List<String> tagNames) {
    if (tagNames == null || tagNames.isEmpty()) {
      return new ArrayList<>();
    }

    List<TagJpaEntity> result = new ArrayList<>();
    for (String tagName : tagNames) {
      TagJpaEntity tagEntity =
          tagJpaRepository
              .findByName(tagName)
              .orElseGet(
                  () -> tagJpaRepository.save(new TagJpaEntity(tagName)));
      result.add(tagEntity);
    }
    return result;
  }

  private Article toDomain(ArticleJpaEntity entity) {
    List<String> tagNames = entity.getTags().stream().map(TagJpaEntity::getName).toList();

    return new Article(
        entity.getId(),
        entity.getSlug(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getBody(),
        entity.getAuthorId(),
        tagNames,
        entity.getFavoritesCount(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
