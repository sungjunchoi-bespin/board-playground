---
doc_type: srs
gate: B
version: v1.1
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: [R-F-01, R-F-02, R-F-03, R-F-04, R-F-05, R-F-06, R-F-07, R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-14, R-F-15, R-F-16, R-F-17, R-F-18, R-F-19, R-N-01, R-N-02, R-N-03, R-N-04, R-N-05, R-N-06]
  F-ID: []
  supersedes: null
---

# Conduit (RealWorld) — SRS

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.1 | 2026-05-18 | Agent (analyst) | 로컬 전용 실행 방침 반영 — 가정·비기능 요구사항 조정 |
| v1.0 | 2026-05-18 | Agent (analyst) | 초안 — RealWorld 스펙 기반 기능·비기능 요구사항 정의 |

## 1. 범위 / 가정

### 범위

- RealWorld 공식 API 스펙 (https://realworld-docs.netlify.app) 100% 준수
- 프론트엔드 SPA + 백엔드 REST API + 관계형 DB 3-tier
- 9개 프론트엔드 라우트, 18개 API 엔드포인트

### 가정

- 기술 스택은 Gate C에서 확정 (프론트엔드·백엔드·DB)
- **로컬 실행 전용**: 개발자 머신(localhost)에서 FE·BE·DB 모두 기동. 클라우드 배포는 현 단계 범위 밖이나, 추후 배포 가능한 아키텍처(환경 변수 분리, 3-tier 구조)를 유지한다
- DB는 로컬 파일 기반(SQLite 등) 우선 고려 — 설치 부담 최소화. ORM/추상 계층으로 추후 PostgreSQL 전환 가능하게 설계
- 이미지 업로드 없음 — URL 입력 방식
- 2024/08/16 이후 변경 사항 반영: List Articles / Feed 엔드포인트에서 body 미반환

## 2. 기능 요구사항

### R-F-01: 회원가입

- **우선순위**: P0
- **설명**: 사용자가 username, email, password로 회원가입하고 JWT를 발급받는다.
- **API**: `POST /api/users` — Body: `{ "user": { "username", "email", "password" } }`
- **Response**: User 객체 (email, token, username, bio, image)
- **Acceptance**: Given 미등록 email·username, When 유효한 username/email/password로 POST /api/users, Then 200 + User 객체 + JWT 발급
- **테스트 시나리오**:
  - 정상: 유효 데이터로 가입 → 200 + JWT 포함 User 객체
  - 실패: 중복 email → 422 에러 (email has already been taken)
  - 실패: 중복 username → 422 에러
  - 실패: 필수 필드 누락 → 422 에러
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-02: 로그인

- **우선순위**: P0
- **설명**: 사용자가 email, password로 로그인하고 JWT를 발급받는다.
- **API**: `POST /api/users/login` — Body: `{ "user": { "email", "password" } }`
- **Acceptance**: Given 등록된 계정, When 올바른 email/password로 POST /api/users/login, Then 200 + JWT 포함 User 객체
- **테스트 시나리오**:
  - 정상: 올바른 credentials → 200 + JWT
  - 실패: 잘못된 password → 401/422 에러
  - 실패: 미등록 email → 401/422 에러
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-03: 현재 사용자 조회

- **우선순위**: P0
- **설명**: 로그인된 사용자가 자기 정보를 조회한다.
- **API**: `GET /api/user` — Header: `Authorization: Token jwt.token.here`
- **Acceptance**: Given 유효 JWT, When GET /api/user, Then 200 + 현재 User 객체
- **테스트 시나리오**:
  - 정상: 유효 JWT로 조회 → 200 + User 객체 (성공)
  - 실패: JWT 없음 → 401 에러
  - 실패: 만료/변조 JWT → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-04: 사용자 정보 수정

- **우선순위**: P1
- **설명**: 로그인된 사용자가 자기 프로필(email, username, password, image, bio)을 수정한다.
- **API**: `PUT /api/user` — Body: `{ "user": { ...fields } }`
- **Acceptance**: Given 로그인 상태, When PUT /api/user with 수정 필드, Then 200 + 갱신된 User 객체
- **테스트 시나리오**:
  - 정상: bio 수정 → 200 + 갱신된 User (성공)
  - 정상: password 수정 → 200 + 이후 로그인 시 새 password 유효
  - 실패: 중복 email → 422 에러
  - 실패: 미인증 → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-05: 프로필 조회

- **우선순위**: P1
- **설명**: username으로 사용자 프로필(username, bio, image, following)을 조회한다.
- **API**: `GET /api/profiles/:username` — Auth: Optional
- **Acceptance**: Given 존재하는 username, When GET /api/profiles/:username, Then 200 + Profile 객체
- **테스트 시나리오**:
  - 정상: 존재하는 프로필 조회 → 200 + Profile (성공)
  - 정상: 로그인 상태로 조회 → following 필드 정확 반영
  - 실패: 미존재 username → 404 에러
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-06: 팔로우

- **우선순위**: P1
- **설명**: 로그인된 사용자가 다른 사용자를 팔로우한다.
- **API**: `POST /api/profiles/:username/follow` — Auth: Required
- **Acceptance**: Given 로그인 + 타인 username, When POST /api/profiles/:username/follow, Then 200 + Profile(following=true)
- **테스트 시나리오**:
  - 정상: 팔로우 → following=true (성공)
  - 실패: 미인증 → 401 에러 (거부)
  - 실패: 미존재 사용자 → 404 에러
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-07: 언팔로우

- **우선순위**: P1
- **설명**: 로그인된 사용자가 팔로우 중인 사용자를 언팔로우한다.
- **API**: `DELETE /api/profiles/:username/follow` — Auth: Required
- **Acceptance**: Given 팔로잉 상태, When DELETE /api/profiles/:username/follow, Then 200 + Profile(following=false)
- **테스트 시나리오**:
  - 정상: 언팔로우 → following=false (성공)
  - 실패: 미인증 → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-08: 아티클 목록 조회

- **우선순위**: P0
- **설명**: 최신순 아티클 목록을 필터(tag, author, favorited) + 페이지네이션(limit, offset)으로 조회한다.
- **API**: `GET /api/articles?tag=&author=&favorited=&limit=20&offset=0` — Auth: Optional
- **참고**: 2024/08/16 이후 body 필드 미반환 (성능)
- **Acceptance**: Given 아티클 존재, When GET /api/articles, Then 200 + articles[] + articlesCount
- **테스트 시나리오**:
  - 정상: 전체 조회 → 최신순 articles + articlesCount (성공)
  - 정상: tag 필터 → 해당 태그 아티클만 반환
  - 정상: limit=5&offset=0 → 5건 반환
  - 실패: 미존재 tag → 빈 배열 (에러 아님, 빈 결과)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-09: 피드 조회

- **우선순위**: P0
- **설명**: 팔로잉 사용자의 아티클을 최신순으로 조회한다.
- **API**: `GET /api/articles/feed?limit=20&offset=0` — Auth: Required
- **Acceptance**: Given 팔로잉 사용자 존재, When GET /api/articles/feed, Then 200 + 팔로잉 사용자의 articles[]
- **테스트 시나리오**:
  - 정상: 팔로잉 사용자 글 → 피드에 포함 (성공)
  - 정상: 팔로잉 0명 → 빈 피드
  - 실패: 미인증 → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-10: 아티클 단건 조회

- **우선순위**: P0
- **설명**: slug로 아티클 상세(본문 포함)를 조회한다.
- **API**: `GET /api/articles/:slug` — Auth: Not required
- **Acceptance**: Given 존재하는 slug, When GET /api/articles/:slug, Then 200 + Article 객체 (body 포함)
- **테스트 시나리오**:
  - 정상: 존재하는 slug → 200 + Article (성공)
  - 실패: 미존재 slug → 404 에러
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-11: 아티클 생성

- **우선순위**: P0
- **설명**: 로그인된 사용자가 새 아티클을 작성한다.
- **API**: `POST /api/articles` — Body: `{ "article": { "title", "description", "body", "tagList"? } }`
- **Acceptance**: Given 로그인 상태, When 유효 데이터로 POST /api/articles, Then 200 + slug 자동 생성된 Article 객체
- **테스트 시나리오**:
  - 정상: 전체 필드 입력 → 200 + Article + slug 생성 (성공)
  - 정상: tagList 생략 → 빈 tagList로 생성
  - 실패: title 누락 → 422 에러
  - 실패: 미인증 → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-12: 아티클 수정

- **우선순위**: P1
- **설명**: 아티클 작성자가 자기 아티클을 수정한다.
- **API**: `PUT /api/articles/:slug` — Body: `{ "article": { title?, description?, body? } }`
- **Acceptance**: Given 로그인 + 자기 아티클, When PUT /api/articles/:slug, Then 200 + 갱신된 Article
- **테스트 시나리오**:
  - 정상: title 수정 → 200 + 갱신된 Article + slug 재생성 (성공)
  - 실패: 타인 아티클 수정 시도 → 403 에러 (거부)
  - 실패: 미인증 → 401 에러
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-13: 아티클 삭제

- **우선순위**: P1
- **설명**: 아티클 작성자가 자기 아티클을 삭제한다.
- **API**: `DELETE /api/articles/:slug` — Auth: Required
- **Acceptance**: Given 로그인 + 자기 아티클, When DELETE /api/articles/:slug, Then 200/204
- **테스트 시나리오**:
  - 정상: 자기 아티클 삭제 → 성공 + 연관 댓글도 삭제
  - 실패: 타인 아티클 삭제 시도 → 403 에러 (거부)
  - 실패: 미존재 slug → 404 에러
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-14: 댓글 추가

- **우선순위**: P1
- **설명**: 로그인된 사용자가 아티클에 댓글을 추가한다.
- **API**: `POST /api/articles/:slug/comments` — Body: `{ "comment": { "body" } }`
- **Acceptance**: Given 로그인 + 존재하는 아티클, When POST 댓글, Then 200 + Comment 객체
- **테스트 시나리오**:
  - 정상: 댓글 작성 → 200 + Comment (성공)
  - 실패: body 빈 문자열 → 422 에러
  - 실패: 미인증 → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-15: 댓글 목록 조회

- **우선순위**: P1
- **설명**: 아티클의 댓글 목록을 조회한다.
- **API**: `GET /api/articles/:slug/comments` — Auth: Optional
- **Acceptance**: Given 댓글 존재, When GET comments, Then 200 + comments[]
- **테스트 시나리오**:
  - 정상: 댓글 조회 → 200 + comments[] (성공)
  - 정상: 댓글 0건 → 빈 배열
  - 실패: 미존재 slug → 404 에러
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-16: 댓글 삭제

- **우선순위**: P1
- **설명**: 댓글 작성자가 자기 댓글을 삭제한다.
- **API**: `DELETE /api/articles/:slug/comments/:id` — Auth: Required
- **Acceptance**: Given 로그인 + 자기 댓글, When DELETE comment, Then 200/204
- **테스트 시나리오**:
  - 정상: 자기 댓글 삭제 → 성공
  - 실패: 타인 댓글 삭제 시도 → 403 에러 (거부)
  - 실패: 미인증 → 401 에러
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-17: 아티클 즐겨찾기

- **우선순위**: P1
- **설명**: 로그인된 사용자가 아티클을 즐겨찾기에 추가한다.
- **API**: `POST /api/articles/:slug/favorite` — Auth: Required
- **Acceptance**: Given 로그인 + 미즐겨찾기 아티클, When POST favorite, Then 200 + Article(favorited=true, favoritesCount+1)
- **테스트 시나리오**:
  - 정상: 즐겨찾기 → favorited=true (성공)
  - 실패: 미인증 → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### R-F-18: 아티클 즐겨찾기 해제

- **우선순위**: P1
- **설명**: 로그인된 사용자가 즐겨찾기를 해제한다.
- **API**: `DELETE /api/articles/:slug/favorite` — Auth: Required
- **Acceptance**: Given 즐겨찾기 상태, When DELETE favorite, Then 200 + Article(favorited=false, favoritesCount-1)
- **테스트 시나리오**:
  - 정상: 즐겨찾기 해제 → favorited=false (성공)
  - 실패: 미인증 → 401 에러 (거부)
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-F-19: 태그 목록 조회

- **우선순위**: P1
- **설명**: 사용되고 있는 태그 목록을 조회한다.
- **API**: `GET /api/tags` — Auth: Not required
- **Acceptance**: Given 아티클에 태그 존재, When GET /api/tags, Then 200 + tags[]
- **테스트 시나리오**:
  - 정상: 태그 조회 → 200 + tags[] (성공)
  - 정상: 아티클 0건 → 빈 배열 (실패 아님)
  - 실패: 서버 에러 → 500
- 단위: ✅ | 통합: ✅ | E2E: N/A

## 3. 비기능 요구사항

### R-N-01: API 응답 형식

- **우선순위**: P0
- **설명**: 모든 API 응답은 `Content-Type: application/json; charset=utf-8`을 포함한다.
- **Acceptance**: Given 임의의 API 호출, When 응답 수신, Then Content-Type 헤더 포함
- **테스트 시나리오**:
  - 정상: 모든 엔드포인트 → application/json charset=utf-8 (성공)
  - 실패: Content-Type 누락 → 검증 실패 (에러)
- 단위: N/A | 통합: ✅ | E2E: N/A

### R-N-02: 에러 응답 형식

- **우선순위**: P0
- **설명**: 검증 실패 시 422 상태 + `{ "errors": { "field": ["message"] } }` 형식으로 응답한다.
- **HTTP 상태 코드**: 401 (Unauthorized), 403 (Forbidden), 404 (Not Found), 422 (Unprocessable Entity)
- **Acceptance**: Given 유효하지 않은 요청, When API 호출, Then 적절한 HTTP 상태 + errors 객체
- **테스트 시나리오**:
  - 정상: 잘못된 입력 → 422 + errors 객체 (성공적인 에러 응답)
  - 실패: 에러 시 errors 키 누락 → 스펙 위반 (에러)
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-N-03: JWT 인증

- **우선순위**: P0
- **설명**: 인증이 필요한 엔드포인트는 `Authorization: Token jwt.token.here` 헤더를 사용한다.
- **Acceptance**: Given 인증 필요 엔드포인트, When 유효 JWT로 요청, Then 정상 응답
- **테스트 시나리오**:
  - 정상: 유효 JWT → 인증 성공 (성공)
  - 실패: JWT 없음 → 401 에러 (거부)
  - 실패: 변조 JWT → 401 에러
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-N-04: CORS

- **우선순위**: P0
- **설명**: 모든 API 엔드포인트에서 CORS를 지원한다. 로컬 실행 시 FE(예: localhost:5173)와 BE(예: localhost:3000) 포트가 다르므로 CORS 필수. 추후 분리 배포에도 동일 설정 재사용.
- **Acceptance**: Given 다른 origin에서의 요청, When API 호출, Then CORS 헤더 포함
- **테스트 시나리오**:
  - 정상: 다른 origin → Access-Control-Allow-Origin 포함 (성공)
  - 실패: CORS 헤더 누락 → 브라우저 차단 (에러)
- 단위: N/A | 통합: ✅ | E2E: N/A

### R-N-05: 페이지네이션

- **우선순위**: P1
- **설명**: 목록 API는 limit (기본 20) + offset (기본 0) 쿼리 파라미터를 지원한다.
- **Acceptance**: Given 아티클 30건 존재, When GET /api/articles?limit=10&offset=0, Then 10건 반환 + articlesCount=30
- **테스트 시나리오**:
  - 정상: limit=10&offset=10 → 10~19번째 아티클 (성공)
  - 실패: 음수 offset → 빈 배열 또는 에러 (예외 처리)
- 단위: ✅ | 통합: ✅ | E2E: N/A

### R-N-06: Slug 자동 생성

- **우선순위**: P1
- **설명**: 아티클 생성 시 title로부터 URL-safe slug를 자동 생성한다.
- **Acceptance**: Given title "How to train your dragon", When 아티클 생성, Then slug = "how-to-train-your-dragon" (또는 유사 형식)
- **테스트 시나리오**:
  - 정상: 영문 title → lowercase-hyphenated slug (성공)
  - 실패: 동일 title 중복 → 고유 slug 보장 (충돌 예외 처리)
- 단위: ✅ | 통합: ✅ | E2E: N/A

## 4. 인터페이스 요구사항

### API 인터페이스

- Base URL: `/api`
- Content-Type: `application/json; charset=utf-8`
- 인증 헤더: `Authorization: Token <jwt>`
- 에러 형식: `{ "errors": { "field": ["message"] } }`

### 응답 객체

| 객체 | 필드 |
|---|---|
| User | email, token, username, bio, image |
| Profile | username, bio, image, following |
| Article | slug, title, description, body, tagList, createdAt, updatedAt, favorited, favoritesCount, author(Profile) |
| Comment | id, createdAt, updatedAt, body, author(Profile) |
| Tags | tags[] (string[]) |

### 프론트엔드 인터페이스

| 라우트 | 화면 |
|---|---|
| `/#/` | 홈 (피드 + 태그) |
| `/#/login` | 로그인 |
| `/#/register` | 회원가입 |
| `/#/settings` | 설정 |
| `/#/editor` | 새 아티클 |
| `/#/editor/:slug` | 아티클 편집 |
| `/#/article/:slug` | 아티클 상세 |
| `/#/profile/:username` | 프로필 |
| `/#/profile/:username/favorites` | 즐겨찾기 |

## 5. 도메인 모델

```
User (1) ──< Follow >── (N) User
User (1) ──< (N) Article
User (1) ──< (N) Comment
Article (1) ──< (N) Comment
Article (N) ──< ArticleTag >── (N) Tag
User (N) ──< Favorite >── (N) Article
```

- **User**: id, email, username, password_hash, bio, image, created_at, updated_at
- **Article**: id, slug, title, description, body, author_id, created_at, updated_at
- **Comment**: id, body, article_id, author_id, created_at, updated_at
- **Tag**: id, name
- **ArticleTag**: article_id, tag_id (다대다)
- **Follow**: follower_id, following_id (자기참조 다대다)
- **Favorite**: user_id, article_id (다대다)

## 6. Open Questions

- Article slug 중복 처리 전략 (suffix 추가 vs UUID 포함)
- 비밀번호 해싱 알고리즘 선택 (bcrypt vs argon2)
- JWT 만료 시간 정책
- DB: SQLite 우선 (로컬 실행 편의) — ORM 추상화로 추후 PostgreSQL 전환 경로 확보
