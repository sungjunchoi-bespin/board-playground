---
doc_type: hld
gate: C
version: v1.0
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: [R-F-01, R-F-02, R-F-03, R-F-04, R-F-05, R-F-06, R-F-07, R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-14, R-F-15, R-F-16, R-F-17, R-F-18, R-F-19, R-N-01, R-N-02, R-N-03, R-N-04, R-N-05, R-N-06]
  F-ID: []
  supersedes: null
---

# Conduit (RealWorld) — High-Level Design (HLD)

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- FE 7모듈 + BE 10모듈 분해, 데이터 흐름, 비기능 대응 정의 |

## 1. 핵심 모듈 / 컴포넌트

| 모듈 | 책임 | 의존 | 08에서 상세 |
|---|---|---|---|
| App Shell (FE) | 라우팅, 전역 레이아웃, 인증 상태, API 클라이언트 | react-router-dom, Auth Module | 08-M-FE-01 |
| Auth Module (FE) | 회원가입(R-F-01), 로그인(R-F-02), 사용자 조회/수정(R-F-03,04), JWT 관리 | App Shell | 08-M-FE-02 |
| Article Module (FE) | 아티클 조회/생성/수정/삭제(R-F-10~13), 즐겨찾기(R-F-17,18), 마크다운 렌더링 | App Shell, Auth, Comment, Tag | 08-M-FE-03 |
| Profile Module (FE) | 프로필 조회(R-F-05), 팔로우/언팔로우(R-F-06,07) | App Shell, Auth, Feed | 08-M-FE-04 |
| Feed Module (FE) | 글로벌 피드(R-F-08), 개인 피드(R-F-09), 페이지네이션(R-N-05) | App Shell, Auth, Tag | 08-M-FE-05 |
| Comment Module (FE) | 댓글 추가/목록/삭제(R-F-14~16) | App Shell, Auth | 08-M-FE-06 |
| Tag Module (FE) | 태그 목록(R-F-19), 태그 클라우드, 필터링 | App Shell | 08-M-FE-07 |
| Auth Controller/Service (BE) | 회원가입/로그인/사용자 CRUD, 비밀번호 해싱, JWT 발급 | JWT MW, Prisma, Error Handler | 08-M-BE-01 |
| User Controller/Service (BE) | 사용자 CRUD 내부 로직, 중복 검증 | Prisma, Error Handler | 08-M-BE-02 |
| Article Controller/Service (BE) | 아티클 CRUD/피드/즐겨찾기, slug 생성(R-N-06), 페이지네이션 | JWT MW, Prisma, Tag, Error Handler | 08-M-BE-03 |
| Comment Controller/Service (BE) | 댓글 CRUD, 권한 검증 | JWT MW, Prisma, Error Handler | 08-M-BE-04 |
| Tag Controller/Service (BE) | 태그 목록, 아티클-태그 연결 관리 | Prisma | 08-M-BE-05 |
| Profile Controller/Service (BE) | 프로필 조회, 팔로우/언팔로우 | JWT MW, Prisma, Error Handler | 08-M-BE-06 |
| Prisma DB Layer (BE) | ORM 스키마, 마이그레이션, DB 접속(SQLite), 쿼리 추상화 | prisma, @prisma/client | 08-M-BE-07 |
| JWT Middleware (BE) | Authorization 헤더 파싱, JWT 검증, req.user 주입 | jsonwebtoken | 08-M-BE-08 |
| Error Handler (BE) | 전역 에러 핸들링, RealWorld 에러 형식(R-N-02) | Express middleware | 08-M-BE-09 |
| CORS Middleware (BE) | CORS 헤더(R-N-04), preflight OPTIONS 처리 | cors (npm) | 08-M-BE-10 |

> 각 모듈의 상세 설계는 08-lld-module-spec에서 fan-out. 아래는 모듈 맵과 계층별 상세이다.

### 1.1 전체 모듈 맵

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Frontend (React + Vite)                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│  │ App Shell│ │Auth Module│ │Article   │ │Profile   │ │Feed      │ │
│  │          │ │          │ │Module    │ │Module    │ │Module    │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
│  ┌──────────┐ ┌──────────┐                                         │
│  │Comment   │ │Tag Module│                                         │
│  │Module    │ │          │                                         │
│  └──────────┘ └──────────┘                                         │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTP (JSON) via /api
┌────────────────────────────▼────────────────────────────────────────┐
│                      Backend (Express + TypeScript)                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │
│  │CORS         │ │JWT          │ │Error Handler│ │Router       │  │
│  │Middleware   │ │Middleware   │ │             │ │             │  │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Controllers: Auth | User | Article | Comment | Tag | Profile │  │
│  └───────────────────────┬───────────────────────────────────────┘  │
│  ┌───────────────────────▼───────────────────────────────────────┐  │
│  │ Services:    Auth | User | Article | Comment | Tag | Profile │  │
│  └───────────────────────┬───────────────────────────────────────┘  │
│  ┌───────────────────────▼───────────────────────────────────────┐  │
│  │                   Prisma DB Layer (ORM)                       │  │
│  └───────────────────────┬───────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────────┘
                           │
                    ┌──────▼──────┐
                    │   SQLite    │
                    │   (File)    │
                    └─────────────┘
```

### 1.2 프론트엔드 모듈

| 모듈 | 책임 | 의존 | 08에서 상세 |
|---|---|---|---|
| App Shell | 라우팅(react-router, hash mode), 전역 레이아웃(Header/Footer), 인증 상태 전역 관리, API 클라이언트(axios/fetch wrapper) | react-router-dom, Auth Module | 08-M-FE-01 |
| Auth Module | 회원가입(R-F-01), 로그인(R-F-02), 현재 사용자 조회(R-F-03), 사용자 정보 수정(R-F-04), JWT localStorage 관리, 로그아웃 | App Shell (API client, Router) | 08-M-FE-02 |
| Article Module | 아티클 단건 조회(R-F-10), 아티클 생성(R-F-11), 아티클 수정(R-F-12), 아티클 삭제(R-F-13), 마크다운 렌더링, 즐겨찾기 토글(R-F-17, R-F-18) | App Shell, Auth Module, Comment Module, Tag Module | 08-M-FE-03 |
| Profile Module | 프로필 조회(R-F-05), 팔로우(R-F-06), 언팔로우(R-F-07), 사용자 아티클/즐겨찾기 탭 표시 | App Shell, Auth Module, Feed Module | 08-M-FE-04 |
| Feed Module | 글로벌 피드(R-F-08), 개인 피드(R-F-09), 태그 필터 피드, 아티클 카드 목록, 페이지네이션(R-N-05) | App Shell, Auth Module, Tag Module | 08-M-FE-05 |
| Comment Module | 댓글 추가(R-F-14), 댓글 목록 조회(R-F-15), 댓글 삭제(R-F-16) | App Shell, Auth Module | 08-M-FE-06 |
| Tag Module | 태그 목록 조회(R-F-19), 사이드바 태그 클라우드, 태그 클릭 필터링 | App Shell | 08-M-FE-07 |

### 1.3 백엔드 모듈

| 모듈 | 책임 | 의존 | 08에서 상세 |
|---|---|---|---|
| Auth Controller/Service | 회원가입 POST /api/users(R-F-01), 로그인 POST /api/users/login(R-F-02), 현재 사용자 GET /api/user(R-F-03), 사용자 수정 PUT /api/user(R-F-04), 비밀번호 해싱(bcrypt), JWT 발급 | JWT Middleware, Prisma DB Layer, Error Handler | 08-M-BE-01 |
| User Controller/Service | 사용자 CRUD 내부 로직, email/username 중복 검증 | Prisma DB Layer, Error Handler | 08-M-BE-02 |
| Article Controller/Service | 아티클 목록 GET /api/articles(R-F-08), 피드 GET /api/articles/feed(R-F-09), 단건 GET /api/articles/:slug(R-F-10), 생성 POST /api/articles(R-F-11), 수정 PUT /api/articles/:slug(R-F-12), 삭제 DELETE /api/articles/:slug(R-F-13), 즐겨찾기 POST/DELETE /api/articles/:slug/favorite(R-F-17, R-F-18), slug 생성(R-N-06), 페이지네이션(R-N-05) | JWT Middleware, Prisma DB Layer, Tag Controller/Service, Error Handler | 08-M-BE-03 |
| Comment Controller/Service | 댓글 추가 POST /api/articles/:slug/comments(R-F-14), 목록 GET /api/articles/:slug/comments(R-F-15), 삭제 DELETE /api/articles/:slug/comments/:id(R-F-16), 권한 검증(자기 댓글만 삭제) | JWT Middleware, Prisma DB Layer, Error Handler | 08-M-BE-04 |
| Tag Controller/Service | 태그 목록 GET /api/tags(R-F-19), 아티클-태그 연결 관리 | Prisma DB Layer | 08-M-BE-05 |
| Profile Controller/Service | 프로필 조회 GET /api/profiles/:username(R-F-05), 팔로우 POST /api/profiles/:username/follow(R-F-06), 언팔로우 DELETE /api/profiles/:username/follow(R-F-07) | JWT Middleware, Prisma DB Layer, Error Handler | 08-M-BE-06 |
| Prisma DB Layer | ORM 스키마 정의(User, Article, Comment, Tag, Follow, Favorite), 마이그레이션 관리, DB 접속(SQLite), 쿼리 추상화 | prisma, @prisma/client | 08-M-BE-07 |
| JWT Middleware | Authorization 헤더 파싱(Token scheme), JWT 검증/디코딩, req.user 주입, 선택적 인증(Optional Auth) 지원 | jsonwebtoken | 08-M-BE-08 |
| Error Handler | 전역 에러 핸들링, RealWorld 에러 형식 변환 { errors: { field: [message] } }(R-N-02), HTTP 상태 코드 매핑(401/403/404/422) | 없음 (Express middleware) | 08-M-BE-09 |
| CORS Middleware | CORS 헤더 설정(R-N-04), Origin 허용 정책, preflight OPTIONS 처리 | cors (npm) | 08-M-BE-10 |

## 2. 모듈 간 데이터 흐름

### 2.1 인증 흐름 (회원가입/로그인)

```
[Browser]
   │
   ▼
[Auth Module (FE)]
   │  POST /api/users  또는  POST /api/users/login
   │  Body: { "user": { "email", "password", "username"? } }
   ▼
[CORS Middleware] → [Router] → [Auth Controller]
                                      │
                                      ▼
                               [Auth Service]
                                 ├─ password 해싱/검증 (bcrypt)
                                 ├─ email/username 중복 검사
                                 └─ JWT 발급
                                      │
                                      ▼
                               [Prisma DB Layer]
                                      │
                                      ▼
                                  [SQLite]
                                      │
                               (User record)
                                      │
                                      ▼
                               [Auth Controller]
                                      │
                                 Response: { "user": { email, token, username, bio, image } }
                                      │
                                      ▼
[Auth Module (FE)]
   ├─ JWT → localStorage 저장
   └─ 전역 상태(currentUser) 갱신 → Header 네비게이션 변경
```

### 2.2 아티클 CRUD 흐름

```
[Article Module / Editor (FE)]
   │  POST /api/articles
   │  Header: Authorization: Token <jwt>
   │  Body: { "article": { "title", "description", "body", "tagList"? } }
   ▼
[CORS Middleware] → [JWT Middleware] → [Router] → [Article Controller]
                         │                              │
                    req.user 주입                        ▼
                                                 [Article Service]
                                                   ├─ slug 생성 (title → slugify, R-N-06)
                                                   ├─ 태그 upsert (Tag Service 위임)
                                                   └─ 권한 검증 (수정/삭제: 작성자만)
                                                        │
                                                        ▼
                                                 [Prisma DB Layer]
                                                   ├─ Article CREATE/UPDATE/DELETE
                                                   ├─ ArticleTag 연결
                                                   └─ 관련 Comment cascade 삭제
                                                        │
                                                        ▼
                                                    [SQLite]
```

### 2.3 피드/목록 조회 흐름

```
[Feed Module (FE)]
   │  GET /api/articles?tag=X&author=Y&favorited=Z&limit=20&offset=0
   │  또는 GET /api/articles/feed (인증 필수)
   ▼
[CORS Middleware] → [JWT Middleware (optional/required)] → [Article Controller]
                                                                │
                                                                ▼
                                                         [Article Service]
                                                           ├─ 필터 조건 조합
                                                           ├─ 페이지네이션 (limit/offset, R-N-05)
                                                           ├─ feed: 팔로잉 사용자 ID 목록 조회
                                                           └─ favorited/following 상태 계산
                                                                │
                                                                ▼
                                                         [Prisma DB Layer]
                                                                │
                                                      Response: { articles[], articlesCount }
                                                                │
                                                                ▼
[Feed Module (FE)]
   ├─ 아티클 카드 목록 렌더링
   └─ 페이지네이션 UI 갱신
```

### 2.4 댓글 흐름

```
[Comment Module (FE)]
   │  POST /api/articles/:slug/comments
   │  Body: { "comment": { "body" } }
   ▼
[CORS Middleware] → [JWT Middleware] → [Comment Controller]
                                            │
                                            ▼
                                      [Comment Service]
                                        ├─ 아티클 존재 여부 검증 (slug → Article)
                                        ├─ 댓글 생성
                                        └─ 삭제 시 권한 검증 (작성자만)
                                            │
                                            ▼
                                      [Prisma DB Layer] → [SQLite]
```

### 2.5 프로필/팔로우 흐름

```
[Profile Module (FE)]
   │  GET /api/profiles/:username
   │  POST /api/profiles/:username/follow
   │  DELETE /api/profiles/:username/follow
   ▼
[CORS Middleware] → [JWT Middleware (optional/required)] → [Profile Controller]
                                                                │
                                                                ▼
                                                         [Profile Service]
                                                           ├─ 사용자 조회 (username)
                                                           ├─ following 상태 계산
                                                           └─ Follow 레코드 CREATE/DELETE
                                                                │
                                                                ▼
                                                         [Prisma DB Layer] → [SQLite]
```

### 2.6 에러 흐름 (횡단 관심사)

```
[Any Controller/Service]
   │  throw AppError(status, field, message)
   ▼
[Error Handler Middleware]
   ├─ 422 Validation Error → { "errors": { "field": ["message"] } }
   ├─ 401 Unauthorized     → { "errors": { "auth": ["unauthorized"] } }
   ├─ 403 Forbidden        → { "errors": { "auth": ["forbidden"] } }
   ├─ 404 Not Found        → { "errors": { "resource": ["not found"] } }
   └─ 500 Internal         → { "errors": { "server": ["internal error"] } }
   ▼
[Response] Content-Type: application/json; charset=utf-8 (R-N-01)
```

## 3. 비기능 대응

| 비기능 R-ID | 대응 전략 | 상세 |
|---|---|---|
| R-N-01 (API 응답 형식) | Express 전역 미들웨어로 Content-Type 강제 | `app.use((req, res, next) => { res.setHeader('Content-Type', 'application/json; charset=utf-8'); next(); })` 패턴. 모든 응답이 JSON 형식을 보장하며, Error Handler에서도 동일 헤더 적용. 통합 테스트에서 모든 엔드포인트 Content-Type 검증 |
| R-N-02 (에러 응답 형식) | 전역 Error Handler 미들웨어 + 커스텀 AppError 클래스 | AppError(statusCode, errors) 클래스를 정의하고 Controller/Service에서 throw. Error Handler가 catch-all로 `{ "errors": { "field": ["message"] } }` 형식 변환. HTTP 상태: 401(Unauthorized), 403(Forbidden), 404(Not Found), 422(Unprocessable Entity). Prisma 에러도 매핑 |
| R-N-03 (JWT 인증) | JWT Middleware + jsonwebtoken 라이브러리 | Authorization 헤더에서 `Token <jwt>` 파싱. HS256 서명 검증 후 req.user에 userId 주입. 필수 인증(auth required)과 선택 인증(auth optional) 두 모드 지원. JWT secret은 환경변수(JWT_SECRET)로 관리. 만료 시간은 환경변수로 설정 가능 (기본 7d) |
| R-N-04 (CORS) | cors npm 패키지 미들웨어 | `cors({ origin: process.env.CORS_ORIGIN \|\| '*' })` 설정. 로컬 개발 시 FE(localhost:5173)와 BE(localhost:3000) 간 cross-origin 허용. preflight OPTIONS 자동 처리. 추후 배포 시 origin 제한으로 보안 강화 가능 |
| R-N-05 (페이지네이션) | Query parameter 파싱 + Prisma skip/take | limit(기본 20, 최대 100) + offset(기본 0) 쿼리 파라미터를 Article Service에서 파싱. Prisma의 `skip`(offset) + `take`(limit)로 변환. 응답에 `articlesCount`(전체 건수) 포함으로 클라이언트 페이지네이션 UI 지원 |
| R-N-06 (Slug 자동 생성) | slugify 라이브러리 + 고유성 보장 | title을 lowercase-hyphenated 형식으로 변환 (예: "How to Train" -> "how-to-train"). 중복 slug 발생 시 suffix 추가 (예: "-1", "-2" 또는 short UUID 접미사). Article 생성/수정(title 변경) 시 모두 적용. Prisma unique 제약으로 DB 레벨 중복 방지 |

## 4. 외부 인터페이스 윤곽

### 4.1 FE-BE 인터페이스

- **프로토콜**: HTTP/1.1 (로컬 실행)
- **Base URL**: `http://localhost:3000/api`
- **Content-Type**: `application/json; charset=utf-8`
- **인증 헤더**: `Authorization: Token <jwt>` (RealWorld 스펙 -- Bearer가 아닌 Token scheme)
- **엔드포인트 수**: 18개 (SRS 04 기준)

### 4.2 FE 외부 의존

| 외부 리소스 | 용도 | 대응 |
|---|---|---|
| Bootstrap 4 CSS | 공식 테마 스타일 | 로컬 번들 우선, CDN fallback (`demo.productionready.io/main.css`) |
| Ionicons | 아이콘 (하트, 설정 등) | CDN (인터넷 연결 가정) |
| Google Fonts | Titillium Web, Source Serif Pro | CDN (인터넷 연결 가정) |

### 4.3 BE 외부 의존

| 외부 시스템 | 용도 | 대응 |
|---|---|---|
| SQLite (파일) | 데이터 영속화 | Prisma ORM 추상화 -- 추후 PostgreSQL 전환 가능. 파일 경로는 환경변수(DATABASE_URL)로 관리 |

### 4.4 pnpm 모노레포 구조 (윤곽)

```
conduit/
  ├── package.json          # 워크스페이스 루트
  ├── pnpm-workspace.yaml   # packages: ["packages/*"]
  ├── packages/
  │   ├── frontend/         # React + Vite SPA
  │   │   ├── package.json
  │   │   ├── vite.config.ts
  │   │   └── src/
  │   └── backend/          # Express + TypeScript API
  │       ├── package.json
  │       ├── tsconfig.json
  │       ├── prisma/
  │       │   └── schema.prisma
  │       └── src/
  └── .env.dev.example      # 환경변수 템플릿
```
