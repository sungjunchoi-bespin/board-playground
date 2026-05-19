---
doc_type: feature-brief
version: v0.1
date: 2026-05-19
status: Draft
issue: "#4"
mode: add
---

# feat-be-shared-security — Intention Brief

## 의도 (1줄)

Spring Security filter chain + JWT 인증 + CORS + GlobalExceptionHandler — RealWorld API 인증/에러 인프라 일괄 구축.

## 배경

Sprint 0 인프라. BE scaffold(#2) 완료 후 모든 API 엔드포인트의 공통 인증·에러 처리·CORS 기반 구축. R-N-01~R-N-04 충족.

## 범위

- SecurityConfig: SecurityFilterChain, CSRF 비활성, stateless session, 공개/인증 경로 분리
- JwtTokenProvider: HS256, 토큰 생성(userId), 파싱/검증
- JwtAuthenticationFilter: OncePerRequestFilter, `Authorization: Token xxx`
- CorsConfig: conduit.cors.allowed-origins 프로퍼티 기반
- GlobalExceptionHandler: 422/401/403/404/500 → `{"errors":{"field":["msg"]}}` RealWorld 형식
- 단위 테스트: JWT 생성/파싱/만료/변조
