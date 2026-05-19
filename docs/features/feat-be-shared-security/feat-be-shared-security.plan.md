---
doc_type: feature-plan
version: v0.1
date: 2026-05-19
status: Draft
issue: "#4"
mode: add
---

# feat-be-shared-security — Implementation Plan

## Subtasks

1. **ConduitProperties** — @ConfigurationProperties for conduit.jwt.* and conduit.cors.*
2. **JwtTokenProvider** — HS256 토큰 생성/파싱/검증
3. **JwtAuthenticationFilter** — OncePerRequestFilter, Token 헤더 파싱
4. **SecurityConfig** — SecurityFilterChain bean, 공개/인증 경로 분리
5. **CorsConfig** — conduit.cors.allowed-origins 기반 CORS 설정
6. **ErrorResponse** — RealWorld 에러 DTO
7. **도메인 예외 클래스** — UnauthorizedException, NotFoundException, etc.
8. **GlobalExceptionHandler** — @RestControllerAdvice, 표준 에러 매핑
9. **단위 테스트** — JWT 생성/파싱/만료/변조 검증
10. **Spotless + 빌드 검증** — ./gradlew spotlessApply && ./gradlew build
