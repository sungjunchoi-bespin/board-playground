package com.conduit.comment.adapter.in.web;

import com.conduit.comment.domain.model.Comment;
import com.conduit.comment.domain.port.in.AddCommentUseCase;
import com.conduit.comment.domain.port.in.DeleteCommentUseCase;
import com.conduit.comment.domain.port.in.ListCommentsUseCase;
import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.out.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles/{slug}/comments")
public class CommentController {

    private final AddCommentUseCase addCommentUseCase;
    private final ListCommentsUseCase listCommentsUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final UserRepository userRepository;

    public CommentController(AddCommentUseCase addCommentUseCase,
                              ListCommentsUseCase listCommentsUseCase,
                              DeleteCommentUseCase deleteCommentUseCase,
                              UserRepository userRepository) {
        this.addCommentUseCase = addCommentUseCase;
        this.listCommentsUseCase = listCommentsUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
        this.userRepository = userRepository;
    }

    public record AddCommentRequest(
            @Valid CommentBody comment
    ) {
        public record CommentBody(
                @NotBlank String body
        ) {}
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable String slug,
            @RequestBody @Valid AddCommentRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Comment comment = addCommentUseCase.addComment(slug, request.comment().body(), userId);
        return ResponseEntity.ok(Map.of("comment", toResponse(comment)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listComments(@PathVariable String slug) {
        List<Comment> comments = listCommentsUseCase.listComments(slug);
        List<Map<String, Object>> responses = comments.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(Map.of("comments", responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String slug,
            @PathVariable Long id,
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
