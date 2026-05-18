---
doc_type: architecture
gate: C
version: v1.0
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: [R-F-01, R-F-02, R-F-03, R-F-04, R-F-05, R-F-06, R-F-07, R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-14, R-F-15, R-F-16, R-F-17, R-F-18, R-F-19, R-N-01, R-N-02, R-N-03, R-N-04, R-N-05, R-N-06]
  F-ID: [F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10]
  supersedes: null
---

# Conduit (RealWorld) — System Architecture

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- 기술 스택 확정, 시스템 컨텍스트/컨테이너 구조 정의 |

## Stack Decision

| 항목 | 결정 | 근거 |
|---|---|---|
| 언어 | TypeScript 5.x (프론트엔드 + 백엔드 공용) | FE/BE 동일 언어로 타입 공유 가능, 정적 타입으로 대규모 리팩터링 안전성 확보. RealWorld API 스펙의 요청/응답 타입을 공유 패키지 없이도 양측에서 동일 interface로 정의 가능 |
| 프레임워크 | React 18 (프론트엔드) + Express 4 (백엔드) | React: 생태계 최대, RealWorld 레퍼런스 구현체 다수. Express: 최소한의 설정으로 REST API 구축, RealWorld API 스펙과 1:1 매핑 용이 |
| 빌드 도구 | Vite 5 (프론트엔드) | esbuild 기반 빠른 HMR, React + TypeScript 기본 지원, 설정 최소 |
| 런타임 | Node.js 20 LTS | LTS 안정성, TypeScript/ESM 네이티브 지원, 2026-04 기준 Active LTS |
| ORM | Prisma 5 | 타입 안전 쿼리, 자동 마이그레이션, SQLite/PostgreSQL 동일 스키마로 전환 가능 (datasource provider 변경만으로 완료) |
| DB | SQLite (로컬 개발) → PostgreSQL (추후 배포) | SQLite: 설치 부담 제로, 파일 기반으로 로컬 실행 즉시 가능. Prisma 추상화로 datasource provider만 교체하면 PostgreSQL 전환 완료 |
| 인증 | JWT (jsonwebtoken 라이브러리) | RealWorld 스펙 요구: `Authorization: Token <jwt>` 헤더 방식. 서버 세션 불필요, 로컬/분리 배포 모두 동일 방식 |
| 마크다운 | marked 라이브러리 | 경량(34KB gzip), GFM 지원, XSS 방지 옵션 내장. 아티클 body 렌더링 전용 |
| 패키지 관리 | pnpm workspace | 모노레포 네이티브 지원, node_modules 중복 제거(하드링크), lockfile 재현성 우수 |
| 테스트 | Vitest (FE) + Vitest (BE) | Vite 생태계 통합, Jest 호환 API, TypeScript 네이티브 지원, 단일 테스트 러너로 FE/BE 통합 |
| 비밀번호 해싱 | bcryptjs | 순수 JS 구현으로 네이티브 빌드 의존 없음, 로컬 환경 호환성 최상 |

## 1. 시스템 컨텍스트

아래 C4 Level 1 (System Context) 다이어그램은 Conduit 시스템과 외부 액터/시스템의 관계를 보여준다.

```
                    +-----------------+
                    |   사용자 (User)  |
                    |   웹 브라우저     |
                    +--------+--------+
                             |
                             | HTTPS (localhost)
                             |
                    +--------v--------+
                    |                 |
                    |  Conduit System |
                    |  (SPA + API +   |
                    |   DB)           |
                    |                 |
                    +--------+--------+
                             |
              +--------------+--------------+
              |              |              |
     +--------v---+  +------v------+  +----v--------+
     | CDN (외부)  |  | Google Fonts|  | Ionicons    |
     | Bootstrap 4 |  | (외부 CDN)  |  | (외부 CDN)  |
     | main.css    |  |             |  |             |
     +-------------+  +-------------+  +-------------+
```

### 액터

| 액터 | 설명 |
|---|---|
| 비회원 독자 | 글로벌 피드 열람, 아티클/프로필 조회, 태그 탐색 |
| 회원 | 인증 후 아티클 CRUD, 댓글, 즐겨찾기, 팔로우, 개인 피드 이용 |
| 개발자 | 로컬 환경에서 시스템 기동, RealWorld 스펙 검증 |

### 외부 자원

| 자원 | 용도 | 장애 시 영향 |
|---|---|---|
| Bootstrap 4 테마 CSS | 공식 RealWorld UI 스타일 | 로컬 번들 fallback 적용 -- 스타일 유지 |
| Google Fonts CDN | Titillium Web, Source Serif Pro | 시스템 폰트 대체 -- 기능 무관 |
| Ionicons CDN | 아이콘 (하트, 설정 등) | 텍스트 대체 -- 기능 무관 |

> 외부 API 의존 없음. 인증(JWT), 데이터(SQLite) 모두 로컬 자체 처리. 클라우드 서비스 의존 제로.

## 2. 컨테이너 구조

C4 Level 2 (Container) 다이어그램. 모노레포 내 두 워크스페이스(frontend, backend)와 데이터 저장소의 관계를 정의한다.

```
+---------------------------------------------------------------------+
|  pnpm monorepo (board-playground/)                                  |
|                                                                     |
|  +-----------------------------+   +-----------------------------+  |
|  |  frontend/                  |   |  backend/                   |  |
|  |  (React 18 SPA)             |   |  (Express REST API)         |  |
|  |                             |   |                             |  |
|  |  - Vite dev server          |   |  - Node.js 20 LTS          |  |
|  |  - React Router (hash)      |   |  - Express 4               |  |
|  |  - TypeScript 5.x           |   |  - TypeScript 5.x          |  |
|  |  - marked (MD render)       |   |  - Prisma 5 ORM            |  |
|  |  - Vitest                   |   |  - jsonwebtoken             |  |
|  |                             |   |  - bcryptjs                 |  |
|  |  localhost:5173             |   |  - cors                     |  |
|  |                             |   |  - Vitest                   |  |
|  |                             |   |                             |  |
|  |                             |   |  localhost:3000              |  |
|  +-------------+---------------+   +-------------+---------------+  |
|                |                                 |                  |
|                |  HTTP (JSON)                     |                  |
|                |  /api/*                          |                  |
|                +--------------->-----------------+                  |
|                                                  |                  |
|                                    +-------------v---------------+  |
|                                    |  SQLite                     |  |
|                                    |  (backend/prisma/dev.db)    |  |
|                                    |                             |  |
|                                    |  Prisma 마이그레이션 관리     |  |
|                                    |  추후 PostgreSQL 전환 가능    |  |
|                                    +-----------------------------+  |
+---------------------------------------------------------------------+
```

### 컨테이너 상세

#### 2.1 Frontend (React SPA)

| 속성 | 값 |
|---|---|
| 기술 | React 18 + TypeScript + Vite |
| 포트 | localhost:5173 (Vite dev server) |
| 라우팅 | Hash-based (`/#/`) -- RealWorld 스펙 준수 |
| 상태 관리 | React Context + useState (외부 상태 라이브러리 없음 -- MVP 범위 충분) |
| API 통신 | fetch API, base URL `/api` (Vite proxy 또는 CORS) |
| 마크다운 | marked 라이브러리로 아티클 body HTML 변환 |
| 인증 | JWT를 localStorage 저장, 요청 시 `Authorization: Token <jwt>` 헤더 부착 |

**주요 디렉터리 구조 (예정)**

```
frontend/
  src/
    components/     # 재사용 UI 컴포넌트
    pages/          # 라우트별 페이지 컴포넌트
    hooks/          # 커스텀 React 훅
    services/       # API 호출 함수 (fetch wrapper)
    types/          # TypeScript 인터페이스/타입
    context/        # React Context (Auth 등)
    utils/          # 유틸리티 함수
    App.tsx         # 라우터 루트
    main.tsx        # 엔트리포인트
  index.html
  vite.config.ts
  tsconfig.json
  vitest.config.ts
```

#### 2.2 Backend (Express REST API)

| 속성 | 값 |
|---|---|
| 기술 | Node.js 20 LTS + Express 4 + TypeScript |
| 포트 | localhost:3000 |
| API prefix | `/api` |
| 인증 | JWT 검증 미들웨어 (jsonwebtoken) |
| ORM | Prisma 5 -- 스키마 기반 타입 안전 쿼리 |
| 비밀번호 | bcryptjs 해싱 |
| CORS | cors 미들웨어 -- FE origin(localhost:5173) 허용 |

**주요 디렉터리 구조 (예정)**

```
backend/
  src/
    routes/         # Express 라우트 정의
    controllers/    # 요청 핸들러 (비즈니스 로직 호출)
    services/       # 비즈니스 로직 (Prisma 호출)
    middleware/     # 인증, 에러 핸들링, 검증
    utils/          # JWT, slug 생성 등 유틸리티
    types/          # TypeScript 인터페이스
    app.ts          # Express 앱 설정
    server.ts       # 서버 엔트리포인트
  prisma/
    schema.prisma   # DB 스키마 정의
    dev.db          # SQLite 데이터 파일 (gitignore)
    migrations/     # Prisma 마이그레이션 파일
  tsconfig.json
  vitest.config.ts
```

#### 2.3 Database (SQLite / Prisma)

| 속성 | 값 |
|---|---|
| 현재 DB | SQLite (파일: `backend/prisma/dev.db`) |
| ORM | Prisma 5 |
| 마이그레이션 | `prisma migrate dev` (개발), `prisma migrate deploy` (적용) |
| 전환 경로 | `schema.prisma`의 `provider = "sqlite"` → `"postgresql"` + connection URL 변경 |

**Prisma 스키마 엔티티 (SRS 도메인 모델 기반)**

```
User        -- id, email, username, passwordHash, bio, image, createdAt, updatedAt
Article     -- id, slug, title, description, body, authorId(→User), createdAt, updatedAt
Comment     -- id, body, articleId(→Article), authorId(→User), createdAt, updatedAt
Tag         -- id, name (unique)
Follow      -- followerId(→User), followingId(→User) -- 복합 PK
Favorite    -- userId(→User), articleId(→Article) -- 복합 PK
ArticleTag  -- implicit many-to-many (Prisma가 자동 관리) 또는 명시적 join table
```

### 2.4 컨테이너 간 통신

| From | To | 프로토콜 | 설명 |
|---|---|---|---|
| 브라우저 | Frontend (Vite) | HTTP | localhost:5173, SPA 정적 자산 서빙 |
| Frontend | Backend | HTTP (JSON) | `/api/*` 엔드포인트, CORS 허용 |
| Backend | SQLite | 파일 I/O | Prisma Client가 SQLite 파일 직접 접근 |
| 브라우저 | 외부 CDN | HTTPS | Bootstrap CSS, Google Fonts, Ionicons |

### 2.5 인증 흐름

```
[브라우저]                [Frontend]              [Backend]              [SQLite]
    |                        |                       |                      |
    |-- 로그인 폼 입력 ------>|                       |                      |
    |                        |-- POST /api/users/login -->                  |
    |                        |                       |-- bcrypt 비교 ------->|
    |                        |                       |<-- User row ---------|
    |                        |                       |-- JWT 서명 (jwtSecret)|
    |                        |<-- { user: { token }} |                      |
    |                        |-- localStorage.set    |                      |
    |                        |   ("jwtToken", token) |                      |
    |                        |                       |                      |
    |-- 인증 필요 요청 ------>|                       |                      |
    |                        |-- GET /api/articles/feed                     |
    |                        |   Authorization: Token <jwt>                 |
    |                        |                       |-- JWT 검증 ---------->|
    |                        |                       |<-- userId ------------|
    |                        |                       |-- 팔로잉 피드 조회 -->|
    |                        |<-- { articles[] }     |                      |
    |<-- 피드 렌더링 ---------|                       |                      |
```

### 2.6 API 엔드포인트 매핑 (RealWorld 스펙)

| Method | Path | Auth | R-ID |
|---|---|---|---|
| POST | /api/users | - | R-F-01 |
| POST | /api/users/login | - | R-F-02 |
| GET | /api/user | Required | R-F-03 |
| PUT | /api/user | Required | R-F-04 |
| GET | /api/profiles/:username | Optional | R-F-05 |
| POST | /api/profiles/:username/follow | Required | R-F-06 |
| DELETE | /api/profiles/:username/follow | Required | R-F-07 |
| GET | /api/articles | Optional | R-F-08 |
| GET | /api/articles/feed | Required | R-F-09 |
| GET | /api/articles/:slug | Optional | R-F-10 |
| POST | /api/articles | Required | R-F-11 |
| PUT | /api/articles/:slug | Required | R-F-12 |
| DELETE | /api/articles/:slug | Required | R-F-13 |
| POST | /api/articles/:slug/comments | Required | R-F-14 |
| GET | /api/articles/:slug/comments | Optional | R-F-15 |
| DELETE | /api/articles/:slug/comments/:id | Required | R-F-16 |
| POST | /api/articles/:slug/favorite | Required | R-F-17 |
| DELETE | /api/articles/:slug/favorite | Required | R-F-18 |
| GET | /api/tags | - | R-F-19 |

### 2.7 배포 확장성 (현재 범위 밖, 설계 시 고려)

현재 단계에서는 로컬 실행만 지원하지만, 추후 배포 시 변경을 최소화하기 위해 다음을 설계에 반영한다.

| 설계 원칙 | 현재 (로컬) | 추후 (배포) |
|---|---|---|
| 환경 변수 분리 | `.env.dev` 로컬 파일 | `.env.stg`, `.env.prod` profile별 |
| DB 추상화 | SQLite + Prisma | PostgreSQL + Prisma (provider 교체) |
| CORS origin | `localhost:5173` | 도메인 URL |
| 포트 설정 | 하드코딩 아닌 `PORT` 환경 변수 | 클라우드 플랫폼 자동 할당 |
| 정적 자산 | Vite dev server | `vite build` → CDN/정적 호스팅 |
| API URL | FE에서 상대 경로 또는 env 변수 | 환경별 API base URL |

## 3. 외부 시스템 / 경계

### 외부 의존 (읽기 전용, 쓰기 없음)

| 시스템 | 유형 | 용도 | 대체 전략 |
|---|---|---|---|
| demo.productionready.io | CDN | Bootstrap 4 테마 CSS | 로컬 번들 포함 (`public/main.css`) |
| fonts.googleapis.com | CDN | Titillium Web, Source Serif Pro | `font-family` fallback 지정 |
| ionic.io (Ionicons) | CDN | UI 아이콘 | 텍스트/유니코드 대체 |

### 보안 경계

| 경계 | 설명 |
|---|---|
| 브라우저 ↔ Backend | JWT 토큰 검증. `Authorization: Token <jwt>` 헤더 필수 (인증 엔드포인트) |
| Backend ↔ SQLite | 로컬 파일 접근. 네트워크 노출 없음. `dev.db`는 gitignore 대상 |
| 비밀번호 | bcryptjs salt round 10 해싱 저장. 평문 저장/로깅 금지 |
| JWT Secret | 환경 변수(`JWT_SECRET`)로 관리. 코드/커밋에 하드코딩 금지 |
| XSS | marked 라이브러리의 `sanitize` 옵션 활성화. 사용자 입력 HTML 이스케이프 |

### 시스템 경계 요약

```
+------------------------------------------------------+
|  로컬 머신 (localhost)                                |
|                                                      |
|  [Browser] ←→ [Vite:5173] ←→ [Express:3000] ←→ [SQLite]
|      |                                               |
|      +--- CDN (Bootstrap/Fonts/Icons) [외부, 읽기전용] |
+------------------------------------------------------+
```

> 전체 시스템이 단일 로컬 머신 내에서 동작한다. 외부 통신은 CDN 자원 로딩뿐이며, 이마저도 로컬 번들 fallback으로 오프라인 동작이 가능하다. 추후 배포 시에도 3-tier(FE/BE/DB) 분리 구조는 그대로 유지되며, 환경 변수와 Prisma datasource provider 변경만으로 전환할 수 있다.
