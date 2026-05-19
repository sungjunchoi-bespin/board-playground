---
doc_type: feature-brief
version: v1.0
date: 2026-05-19
status: Draft
author: Agent (developer)
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# feat-be-scaffold — Intention Brief

## Issue
GitHub Issue #2: `chore(backend): BE 프로젝트 스캐폴딩 — Hexagonal + Spring Boot 3`

## Intent (1-line)
Spring Boot 3.x + Java 24 + Hexagonal Architecture + Spring Modulith 기반 backend 프로젝트 골격을 `backend/` 디렉토리에 생성하여 `./gradlew build` 성공 상태를 만든다.

## Mode
add (scaffolding chore — 신규 프로젝트 구조 생성)

## Scope
- `backend/` 디렉토리에 Gradle Kotlin DSL 프로젝트 생성
- 5개 도메인 모듈(user, article, comment, profile, tag) + shared 패키지 구조
- Spring Boot 3.x, Spring Security, JPA, Flyway, PostgreSQL, Springdoc OpenAPI 의존성
- application-{dev,stg,prod}.yml 3 profile 설정
- Gradle Wrapper 포함
- `./gradlew build` 및 `./gradlew test` 성공

## Out of Scope
- 비즈니스 로직 구현 (후속 Sprint 1+ 이슈)
- Flyway 초기 마이그레이션 (Issue #5 be-db-init)
- Security 설정 (Issue #4 be-shared-security)
- 실제 Controller/Service/Repository 코드 (후속 이슈)
