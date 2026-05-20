package com.conduit.article.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Article update payload (all fields optional)")
public record UpdateArticleRequest(
    @Schema(description = "New title", example = "Updated title") String title,
    @Schema(description = "New description", example = "Updated summary") String description,
    @Schema(description = "New body in Markdown", example = "Updated body content") String body) {}
