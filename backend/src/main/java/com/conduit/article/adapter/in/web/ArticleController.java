package com.conduit.article.adapter.in.web;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.in.CreateArticleUseCase;
import com.conduit.article.domain.port.in.DeleteArticleUseCase;
import com.conduit.article.domain.port.in.FavoriteArticleUseCase;
import com.conduit.article.domain.port.in.FeedArticlesUseCase;
import com.conduit.article.domain.port.in.GetArticleUseCase;
import com.conduit.article.domain.port.in.ListArticlesUseCase;
import com.conduit.article.domain.port.in.UnfavoriteArticleUseCase;
import com.conduit.article.domain.port.in.UpdateArticleUseCase;
import com.conduit.article.domain.port.out.FavoriteRepository;
import com.conduit.article.domain.port.out.FollowRepository;
import com.conduit.shared.exception.ApiException;
import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.out.UserRepository;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ArticleController {

  private final CreateArticleUseCase createArticleUseCase;
  private final GetArticleUseCase getArticleUseCase;
  private final UpdateArticleUseCase updateArticleUseCase;
  private final DeleteArticleUseCase deleteArticleUseCase;
  private final ListArticlesUseCase listArticlesUseCase;
  private final FeedArticlesUseCase feedArticlesUseCase;
  private final FavoriteArticleUseCase favoriteArticleUseCase;
  private final UnfavoriteArticleUseCase unfavoriteArticleUseCase;
  private final UserRepository userRepository;
  private final FavoriteRepository favoriteRepository;
  private final FollowRepository followRepository;

  public ArticleController(
      CreateArticleUseCase createArticleUseCase,
      GetArticleUseCase getArticleUseCase,
      UpdateArticleUseCase updateArticleUseCase,
      DeleteArticleUseCase deleteArticleUseCase,
      ListArticlesUseCase listArticlesUseCase,
      FeedArticlesUseCase feedArticlesUseCase,
      FavoriteArticleUseCase favoriteArticleUseCase,
      UnfavoriteArticleUseCase unfavoriteArticleUseCase,
      UserRepository userRepository,
      FavoriteRepository favoriteRepository,
      FollowRepository followRepository) {
    this.createArticleUseCase = createArticleUseCase;
    this.getArticleUseCase = getArticleUseCase;
    this.updateArticleUseCase = updateArticleUseCase;
    this.deleteArticleUseCase = deleteArticleUseCase;
    this.listArticlesUseCase = listArticlesUseCase;
    this.feedArticlesUseCase = feedArticlesUseCase;
    this.favoriteArticleUseCase = favoriteArticleUseCase;
    this.unfavoriteArticleUseCase = unfavoriteArticleUseCase;
    this.userRepository = userRepository;
    this.favoriteRepository = favoriteRepository;
    this.followRepository = followRepository;
  }

  @PostMapping("/articles")
  public ResponseEntity<Map<String, ArticleResponse>> createArticle(
      Authentication authentication,
      @Valid @RequestBody Map<String, CreateArticleRequest> body) {
    Long userId = (Long) authentication.getPrincipal();
    CreateArticleRequest req = body.get("article");
    Article article =
        createArticleUseCase.create(
            req.title(), req.description(), req.body(), userId, req.tagList());
    User author = findAuthor(article.getAuthorId());
    return ResponseEntity.ok(Map.of("article", ArticleResponse.from(article, author)));
  }

  @GetMapping("/articles")
  public ResponseEntity<Map<String, Object>> listArticles(
      Authentication authentication,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) String author,
      @RequestParam(required = false) String favorited,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    List<Article> articles =
        listArticlesUseCase.listArticles(tag, author, favorited, limit, offset);
    long articlesCount = listArticlesUseCase.countArticles(tag, author, favorited);

    Long currentUserId = getCurrentUserId(authentication);
    List<ArticleResponse> responses = buildArticleResponses(articles, currentUserId);

    return ResponseEntity.ok(Map.of("articles", responses, "articlesCount", articlesCount));
  }

  @GetMapping("/articles/feed")
  public ResponseEntity<Map<String, Object>> feedArticles(
      Authentication authentication,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    Long userId = (Long) authentication.getPrincipal();

    List<Article> articles = feedArticlesUseCase.feedArticles(userId, limit, offset);
    long articlesCount = feedArticlesUseCase.countFeedArticles(userId);

    List<ArticleResponse> responses = buildArticleResponses(articles, userId);

    return ResponseEntity.ok(Map.of("articles", responses, "articlesCount", articlesCount));
  }

  @GetMapping("/articles/{slug}")
  public ResponseEntity<Map<String, ArticleResponse>> getArticle(@PathVariable String slug) {
    Article article = getArticleUseCase.getBySlug(slug);
    User author = findAuthor(article.getAuthorId());
    return ResponseEntity.ok(Map.of("article", ArticleResponse.from(article, author)));
  }

  @PutMapping("/articles/{slug}")
  public ResponseEntity<Map<String, ArticleResponse>> updateArticle(
      Authentication authentication,
      @PathVariable String slug,
      @RequestBody Map<String, UpdateArticleRequest> body) {
    Long userId = (Long) authentication.getPrincipal();
    UpdateArticleRequest req = body.get("article");
    Article article =
        updateArticleUseCase.update(slug, userId, req.title(), req.description(), req.body());
    User author = findAuthor(article.getAuthorId());
    return ResponseEntity.ok(Map.of("article", ArticleResponse.from(article, author)));
  }

  @DeleteMapping("/articles/{slug}")
  public ResponseEntity<Void> deleteArticle(
      Authentication authentication, @PathVariable String slug) {
    Long userId = (Long) authentication.getPrincipal();
    deleteArticleUseCase.delete(slug, userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/articles/{slug}/favorite")
  public ResponseEntity<Map<String, ArticleResponse>> favoriteArticle(
      Authentication authentication, @PathVariable String slug) {
    Long userId = (Long) authentication.getPrincipal();
    Article article = favoriteArticleUseCase.favorite(slug, userId);
    User author = findAuthor(article.getAuthorId());
    boolean isFollowing = followRepository.existsByFollowerIdAndFolloweeId(userId, article.getAuthorId());
    return ResponseEntity.ok(Map.of("article", ArticleResponse.from(article, author, true, isFollowing)));
  }

  @DeleteMapping("/articles/{slug}/favorite")
  public ResponseEntity<Map<String, ArticleResponse>> unfavoriteArticle(
      Authentication authentication, @PathVariable String slug) {
    Long userId = (Long) authentication.getPrincipal();
    Article article = unfavoriteArticleUseCase.unfavorite(slug, userId);
    User author = findAuthor(article.getAuthorId());
    boolean isFollowing = followRepository.existsByFollowerIdAndFolloweeId(userId, article.getAuthorId());
    return ResponseEntity.ok(Map.of("article", ArticleResponse.from(article, author, false, isFollowing)));
  }

  private List<ArticleResponse> buildArticleResponses(List<Article> articles, Long currentUserId) {
    if (articles.isEmpty()) {
      return List.of();
    }

    List<Long> articleIds = articles.stream().map(Article::getId).toList();
    List<Long> authorIds = articles.stream().map(Article::getAuthorId).distinct().toList();

    Set<Long> favoritedArticleIds =
        currentUserId != null
            ? favoriteRepository.findFavoritedArticleIds(currentUserId, articleIds)
            : Set.of();

    Set<Long> followedAuthorIds =
        currentUserId != null
            ? followRepository.findFollowedUserIds(currentUserId, authorIds)
            : Set.of();

    Map<Long, User> authorMap =
        authorIds.stream()
            .map(id -> userRepository.findById(id).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toMap(User::getId, Function.identity()));

    List<ArticleResponse> responses = new ArrayList<>();
    for (Article article : articles) {
      User author = authorMap.get(article.getAuthorId());
      if (author == null) {
        continue;
      }
      boolean isFavorited = favoritedArticleIds.contains(article.getId());
      boolean isFollowing = followedAuthorIds.contains(article.getAuthorId());
      responses.add(ArticleResponse.from(article, author, isFavorited, isFollowing));
    }
    return responses;
  }

  private Long getCurrentUserId(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
      return userId;
    }
    return null;
  }

  private User findAuthor(Long authorId) {
    return userRepository
        .findById(authorId)
        .orElseThrow(() -> new ApiException.NotFoundException("author not found"));
  }
}
