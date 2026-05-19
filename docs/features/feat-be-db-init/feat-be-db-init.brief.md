---
doc_type: feature-brief
version: v0.1
date: 2026-05-19
status: Draft
issue: "#5"
mode: add
---

# feat-be-db-init — Intention Brief

## 의도 (1줄)

Flyway V1 초기 스키마 마이그레이션(7 테이블) + JPA Entity 기본 클래스 + Testcontainers PostgreSQL 통합 테스트.

## 범위

- V1__init_schema.sql: users, articles, comments, tags, article_tags, follows, favorites
- BaseEntity: id, createdAt, updatedAt 공통 추출
- Testcontainers PostgreSQL @TestConfiguration
- Flyway 마이그레이션 자동 실행 검증
