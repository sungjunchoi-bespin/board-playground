package com.conduit.article.domain.model;

public record ArticleWithMeta(Article article, boolean favorited, boolean following) {}
