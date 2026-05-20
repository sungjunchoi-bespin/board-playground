package com.conduit.article.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "Article creation payload")
public record CreateArticleRequest(
    @NotBlank(message = "can't be blank")
        @Schema(description = "Article title", example = "How to train your dragon")
        String title,
    @NotBlank(message = "can't be blank")
        @Schema(description = "Short summary of the article", example = "Ever wonder how?")
        String description,
    @NotBlank(message = "can't be blank")
        @Schema(
            description = "Article body in Markdown",
            example = "## Introduction\n\nIt takes a Toothless...")
        String body,
    @Schema(description = "List of tags", example = "[\"dragons\", \"training\"]")
        List<String> tagList) {}
