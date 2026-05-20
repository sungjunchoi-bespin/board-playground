package com.conduit.comment.adapter.in.web;

import com.conduit.comment.domain.model.Comment;
import com.conduit.comment.domain.port.in.AddCommentUseCase;
import com.conduit.comment.domain.port.in.DeleteCommentUseCase;
import com.conduit.comment.domain.port.in.ListCommentsUseCase;
import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.out.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comments", description = "Comments on articles")
@RestController
@RequestMapping("/api/articles/{slug}/comments")
public class CommentController {

  private final AddCommentUseCase addCommentUseCase;
  private final ListCommentsUseCase listCommentsUseCase;
  private final DeleteCommentUseCase deleteCommentUseCase;
  private final UserRepository userRepository;

  public CommentController(
      AddCommentUseCase addCommentUseCase,
      ListCommentsUseCase listCommentsUseCase,
      DeleteCommentUseCase deleteCommentUseCase,
      UserRepository userRepository) {
    this.addCommentUseCase = addCommentUseCase;
    this.listCommentsUseCase = listCommentsUseCase;
    this.deleteCommentUseCase = deleteCommentUseCase;
    this.userRepository = userRepository;
  }

  public record AddCommentRequest(@Valid CommentBody comment) {
    public record CommentBody(@NotBlank String body) {}
  }

  @Operation(
      summary = "Add comment to an article",
      description = "Add a comment to an article. Auth required.",
      security = @SecurityRequirement(name = "Token"))
  @ApiResponse(responseCode = "200", description = "Comment created")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "404", description = "Article not found")
  @PostMapping
  public ResponseEntity<Map<String, Object>> addComment(
      @Parameter(description = "Slug of the article") @PathVariable String slug,
      @RequestBody @Valid AddCommentRequest request,
      Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    Comment comment = addCommentUseCase.addComment(slug, request.comment().body(), userId);
    return ResponseEntity.ok(Map.of("comment", toResponse(comment)));
  }

  @Operation(
      summary = "List comments for an article",
      description = "Get all comments for an article. No auth required.")
  @ApiResponse(responseCode = "200", description = "List of comments")
  @GetMapping
  public ResponseEntity<Map<String, Object>> listComments(
      @Parameter(description = "Slug of the article") @PathVariable String slug) {
    List<Comment> comments = listCommentsUseCase.listComments(slug);
    List<Map<String, Object>> responses = comments.stream().map(this::toResponse).toList();
    return ResponseEntity.ok(Map.of("comments", responses));
  }

  @Operation(
      summary = "Delete a comment",
      description = "Delete a comment. Auth required. Only the comment author can delete.",
      security = @SecurityRequirement(name = "Token"))
  @ApiResponse(responseCode = "200", description = "Comment deleted")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden — not the comment author")
  @ApiResponse(responseCode = "404", description = "Comment not found")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(
      @Parameter(description = "Slug of the article") @PathVariable String slug,
      @Parameter(description = "Comment ID") @PathVariable Long id,
      Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    deleteCommentUseCase.deleteComment(slug, id, userId);
    return ResponseEntity.ok().build();
  }

  private Map<String, Object> toResponse(Comment comment) {
    User author = userRepository.findById(comment.authorId()).orElse(null);

    Map<String, Object> authorMap = new LinkedHashMap<>();
    if (author != null) {
      authorMap.put("username", author.getUsername());
      authorMap.put("bio", author.getBio() != null ? author.getBio() : "");
      authorMap.put("image", author.getImage() != null ? author.getImage() : "");
      authorMap.put("following", false);
    }

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("id", comment.id());
    response.put("createdAt", comment.createdAt().toString());
    response.put("updatedAt", comment.updatedAt().toString());
    response.put("body", comment.body());
    response.put("author", authorMap);
    return response;
  }

  private Long getCurrentUserId(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
      return userId;
    }
    return null;
  }
}
