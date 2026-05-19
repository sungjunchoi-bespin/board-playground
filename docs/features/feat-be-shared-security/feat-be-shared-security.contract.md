---
doc_type: feature-contract
version: v0.1
date: 2026-05-19
status: Draft
issue: "#4"
mode: add
---

# feat-be-shared-security — Change Contract

## §0 Referenced-IDs

- R-ID: R-N-01, R-N-02, R-N-03, R-N-04
- F-ID: (none)
- ADR: (none)
- Blocked-by: #2 (be-scaffold, resolved)
- Supersedes: (none)

## §1 변경 요약

| 항목 | Before | After |
|---|---|---|
| Spring Security | 의존성만 존재, 설정 없음 | SecurityFilterChain + JWT 필터 + stateless session |
| JWT | N/A | JwtTokenProvider (HS256, generate/parse/validate) |
| 인증 필터 | N/A | JwtAuthenticationFilter (`Authorization: Token xxx`) |
| CORS | N/A | CorsConfig (conduit.cors.allowed-origins) |
| 에러 처리 | N/A | GlobalExceptionHandler (RealWorld 형식 422/401/403/404/500) |
| 에러 DTO | N/A | ErrorResponse `{"errors":{"field":["msg"]}}` |
| 프로퍼티 | conduit.jwt/cors 미사용 | ConduitProperties @ConfigurationProperties 바인딩 |
| 테스트 | contextLoads only | JWT 단위 테스트 추가 |

## §2 영향 분석

- `shared/security/`, `shared/exception/`, `shared/config/` 패키지에 신규 클래스 추가
- 기존 코드 변경: 없음 (신규 추가만)
- Breaking change: 없음
