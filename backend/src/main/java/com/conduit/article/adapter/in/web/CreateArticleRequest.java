package com.conduit.article.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateArticleRequest(
    @NotBlank(message = "can't be blank") String title,
    @NotBlank(message = "can't be blank") String description,
    @NotBlank(message = "can't be blank") String body,
    List<String> tagList) {}
