package com.conduit.comment.domain.model;

import java.time.Instant;

public record Comment(
    Long id, String body, Long articleId, Long authorId, Instant createdAt, Instant updatedAt) {}
