package com.conduit.article.adapter.in.web;

import com.conduit.article.domain.model.Article;
import com.conduit.article.domain.port.in.CreateArticleUseCase;
import com.conduit.article.domain.port.in.DeleteArticleUseCase;
import com.conduit.article.domain.port.in.GetArticleUseCase;
import com.conduit.article.domain.port.in.UpdateArticleUseCase;
import com.conduit.shared.exception.ApiException;
import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.out.UserRepository;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ArticleController {

  private final CreateArticleUseCase createArticleUseCase;
  private final GetArticleUseCase getArticleUseCase;
  private final UpdateArticleUseCase updateArticleUseCase;
  private final DeleteArticleUseCase deleteArticleUseCase;
  private final UserRepository userRepository;

  public ArticleController(
      CreateArticleUseCase createArticleUseCase,
      GetArticleUseCase getArticleUseCase,
      UpdateArticleUseCase updateArticleUseCase,
      DeleteArticleUseCase deleteArticleUseCase,
      UserRepository userRepository) {
    this.createArticleUseCase = createArticleUseCase;
    this.getArticleUseCase = getArticleUseCase;
    this.updateArticleUseCase = updateArticleUseCase;
    this.deleteArticleUseCase = deleteArticleUseCase;
    this.userRepository = userRepository;
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

  private User findAuthor(Long authorId) {
    return userRepository
        .findById(authorId)
        .orElseThrow(() -> new ApiException.NotFoundException("author not found"));
  }
}
