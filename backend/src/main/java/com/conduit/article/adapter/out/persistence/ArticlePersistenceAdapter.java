package com.conduit.article.adapter.out.persistence;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.out.ArticleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ArticlePersistenceAdapter implements ArticleRepository {

  private final ArticleJpaRepository articleJpaRepository;
  private final TagJpaRepository tagJpaRepository;

  @PersistenceContext private EntityManager entityManager;

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
    articleJpaRepository.findById(article.getId()).ifPresent(articleJpaRepository::delete);
  }

  @Override
  public boolean existsBySlug(String slug) {
    return articleJpaRepository.existsBySlug(slug);
  }

  @Override
  public List<Article> findAll(
      String tag, String authorUsername, String favoritedByUsername, int limit, int offset) {
    StringBuilder jpql = new StringBuilder("SELECT DISTINCT a FROM ArticleJpaEntity a");
    List<String> joins = new ArrayList<>();
    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isBlank()) {
      joins.add("JOIN a.tags t");
      conditions.add("t.name = :tag");
    }
    if (authorUsername != null && !authorUsername.isBlank()) {
      joins.add(
          "JOIN com.conduit.user.adapter.out.persistence.UserJpaEntity u ON a.authorId = u.id");
      conditions.add("u.username = :authorUsername");
    }
    if (favoritedByUsername != null && !favoritedByUsername.isBlank()) {
      joins.add(
          "JOIN com.conduit.user.adapter.out.persistence.UserJpaEntity fu ON fu.username = :favoritedByUsername");
      joins.add(
          "JOIN FavoriteJpaEntity fav ON fav.articleId = a.id AND fav.userId = fu.id");
    }

    for (String join : joins) {
      jpql.append(" ").append(join);
    }

    if (!conditions.isEmpty()) {
      jpql.append(" WHERE ").append(String.join(" AND ", conditions));
    }

    jpql.append(" ORDER BY a.createdAt DESC");

    TypedQuery<ArticleJpaEntity> query =
        entityManager.createQuery(jpql.toString(), ArticleJpaEntity.class);

    if (tag != null && !tag.isBlank()) {
      query.setParameter("tag", tag);
    }
    if (authorUsername != null && !authorUsername.isBlank()) {
      query.setParameter("authorUsername", authorUsername);
    }
    if (favoritedByUsername != null && !favoritedByUsername.isBlank()) {
      query.setParameter("favoritedByUsername", favoritedByUsername);
    }

    query.setFirstResult(offset);
    query.setMaxResults(limit);

    return query.getResultList().stream().map(this::toDomain).toList();
  }

  @Override
  public long countAll(String tag, String authorUsername, String favoritedByUsername) {
    StringBuilder jpql = new StringBuilder("SELECT COUNT(DISTINCT a.id) FROM ArticleJpaEntity a");
    List<String> joins = new ArrayList<>();
    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isBlank()) {
      joins.add("JOIN a.tags t");
      conditions.add("t.name = :tag");
    }
    if (authorUsername != null && !authorUsername.isBlank()) {
      joins.add(
          "JOIN com.conduit.user.adapter.out.persistence.UserJpaEntity u ON a.authorId = u.id");
      conditions.add("u.username = :authorUsername");
    }
    if (favoritedByUsername != null && !favoritedByUsername.isBlank()) {
      joins.add(
          "JOIN com.conduit.user.adapter.out.persistence.UserJpaEntity fu ON fu.username = :favoritedByUsername");
      joins.add(
          "JOIN FavoriteJpaEntity fav ON fav.articleId = a.id AND fav.userId = fu.id");
    }

    for (String join : joins) {
      jpql.append(" ").append(join);
    }

    if (!conditions.isEmpty()) {
      jpql.append(" WHERE ").append(String.join(" AND ", conditions));
    }

    TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);

    if (tag != null && !tag.isBlank()) {
      query.setParameter("tag", tag);
    }
    if (authorUsername != null && !authorUsername.isBlank()) {
      query.setParameter("authorUsername", authorUsername);
    }
    if (favoritedByUsername != null && !favoritedByUsername.isBlank()) {
      query.setParameter("favoritedByUsername", favoritedByUsername);
    }

    return query.getSingleResult();
  }

  @Override
  public List<Article> findByAuthorIds(List<Long> authorIds, int limit, int offset) {
    if (authorIds == null || authorIds.isEmpty()) {
      return List.of();
    }
    TypedQuery<ArticleJpaEntity> query =
        entityManager.createQuery(
            "SELECT a FROM ArticleJpaEntity a WHERE a.authorId IN :authorIds ORDER BY a.createdAt DESC",
            ArticleJpaEntity.class);
    query.setParameter("authorIds", authorIds);
    query.setFirstResult(offset);
    query.setMaxResults(limit);
    return query.getResultList().stream().map(this::toDomain).toList();
  }

  @Override
  public long countByAuthorIds(List<Long> authorIds) {
    if (authorIds == null || authorIds.isEmpty()) {
      return 0;
    }
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(a) FROM ArticleJpaEntity a WHERE a.authorId IN :authorIds", Long.class);
    query.setParameter("authorIds", authorIds);
    return query.getSingleResult();
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
              .orElseGet(() -> tagJpaRepository.save(new TagJpaEntity(tagName)));
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
