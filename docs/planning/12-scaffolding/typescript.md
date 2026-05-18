---
doc_type: scaffolding
gate: C
version: v1.0
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# Conduit (RealWorld) — Scaffolding

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- TypeScript pnpm monorepo scaffolding (FE: React+Vite, BE: Express+Prisma) |

## 1. 디렉토리 트리

pnpm workspace 기반 monorepo. 각 workspace가 독립 `.env` 파일을 보유한다 (pattern (e)).

```
conduit/
├── frontend/                        # React + Vite (FE workspace)
│   ├── src/
│   │   ├── components/              # 재사용 UI 컴포넌트
│   │   │   ├── article-card.tsx
│   │   │   ├── article-card.module.css
│   │   │   ├── header.tsx
│   │   │   └── ...
│   │   ├── pages/                   # 라우트 단위 페이지 컴포넌트
│   │   │   ├── home-page.tsx
│   │   │   ├── login-page.tsx
│   │   │   ├── register-page.tsx
│   │   │   ├── article-page.tsx
│   │   │   ├── editor-page.tsx
│   │   │   ├── settings-page.tsx
│   │   │   ├── profile-page.tsx
│   │   │   └── ...
│   │   ├── hooks/                   # 커스텀 React 훅
│   │   │   ├── use-auth.ts
│   │   │   ├── use-articles.ts
│   │   │   └── ...
│   │   ├── api/                     # API 호출 함수 (fetch wrapper)
│   │   │   ├── client.ts            # base fetch wrapper (JWT 주입)
│   │   │   ├── auth-api.ts
│   │   │   ├── article-api.ts
│   │   │   └── ...
│   │   ├── types/                   # 공유 타입 정의
│   │   │   ├── article.ts
│   │   │   ├── user.ts
│   │   │   └── ...
│   │   ├── styles/                  # 글로벌 스타일, 디자인 토큰
│   │   │   └── global.css
│   │   ├── app.tsx                  # 라우터 + 레이아웃
│   │   └── main.tsx                 # entrypoint (stylesheet import)
│   ├── public/
│   ├── .env.dev.example
│   ├── .env.stg.example
│   ├── .env.prod.example
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
├── backend/                         # Express + Prisma (BE workspace)
│   ├── src/
│   │   ├── controllers/             # HTTP 요청 처리, DTO 변환
│   │   │   ├── auth-controller.ts
│   │   │   ├── article-controller.ts
│   │   │   ├── comment-controller.ts
│   │   │   ├── profile-controller.ts
│   │   │   ├── tag-controller.ts
│   │   │   └── ...
│   │   ├── services/                # 비즈니스 로직, Prisma 호출
│   │   │   ├── auth-service.ts
│   │   │   ├── article-service.ts
│   │   │   ├── comment-service.ts
│   │   │   ├── profile-service.ts
│   │   │   ├── tag-service.ts
│   │   │   └── ...
│   │   ├── middlewares/             # Express 미들웨어
│   │   │   ├── auth-middleware.ts
│   │   │   ├── error-handler.ts
│   │   │   ├── validation-middleware.ts
│   │   │   └── ...
│   │   ├── routes/                  # 라우트 정의 (controller 연결)
│   │   │   ├── auth-routes.ts
│   │   │   ├── article-routes.ts
│   │   │   ├── comment-routes.ts
│   │   │   ├── profile-routes.ts
│   │   │   ├── tag-routes.ts
│   │   │   └── index.ts            # 라우트 집합
│   │   ├── prisma/                  # Prisma client singleton
│   │   │   └── client.ts
│   │   ├── types/                   # DTO, 요청/응답 타입
│   │   │   ├── article-dto.ts
│   │   │   ├── user-dto.ts
│   │   │   └── ...
│   │   ├── utils/                   # 유틸리티 함수
│   │   │   ├── jwt.ts
│   │   │   ├── slug.ts
│   │   │   └── ...
│   │   └── app.ts                   # Express app 설정 + 서버 기동
│   ├── prisma/
│   │   ├── schema.prisma            # DB 스키마 정의
│   │   └── migrations/              # 운영 migration 파일 (stg/prod)
│   ├── .env.dev.example
│   ├── .env.stg.example
│   ├── .env.prod.example
│   ├── package.json
│   └── tsconfig.json
├── package.json                     # root -- workspaces 정의, lint-staged
├── pnpm-workspace.yaml              # workspace 패키지 목록
├── pnpm-lock.yaml                   # lockfile
├── eslint.config.js                 # ESLint 공통 설정
├── .prettierrc                      # Prettier 공통 설정
├── tsconfig.json                    # base tsconfig (extends용)
└── LOCAL.md                         # 부팅 사용자 가이드 (ADR-0040)
```

## 2. 패키지 명명 규칙

| 범위 | 규칙 | 예 |
|---|---|---|
| root workspace | `conduit` | `package.json` > `"name": "conduit"` |
| frontend workspace | `@conduit/frontend` | `frontend/package.json` > `"name": "@conduit/frontend"` |
| backend workspace | `@conduit/backend` | `backend/package.json` > `"name": "@conduit/backend"` |
| pnpm filter | `--filter` + workspace name | `pnpm --filter @conduit/backend dev` |
| pnpm-workspace.yaml | packages 목록 | `packages: ["frontend", "backend"]` |

- scope는 `@conduit/`로 통일한다.
- 향후 shared 패키지 추가 시 `@conduit/shared`로 확장 가능.
- pnpm `--filter` 실행 시 workspace name 또는 디렉토리명을 사용한다. 본 문서에서는 간결성을 위해 디렉토리명(`backend`, `frontend`)을 사용한다.

## 3. 디자인 패턴 결정

### Backend -- Layered Architecture

**선택 패턴**: Layered

**이유**: RealWorld 스펙은 REST API 기반의 CRUD 도메인으로, 복잡한 도메인 로직보다 요청-응답 파이프라인이 핵심이다. Layered 패턴은 Controller -> Service -> Repository(Prisma) 3계층으로 관심사를 분리하면서도 학습 곡선이 낮고, Express + Prisma 스택과 자연스럽게 매핑된다.

- **Controller 계층**: HTTP 요청 파싱, 입력 유효성 검증, DTO 변환, 응답 포맷팅
- **Service 계층**: 비즈니스 로직, 트랜잭션 관리, 에러 발생
- **Repository 계층**: Prisma Client 호출 (Service에서 직접 사용, 별도 Repository 클래스 생략 -- Prisma가 충분한 추상화 제공)

### Frontend -- Component-based Architecture

**선택 패턴**: Layered (Component-based)

**이유**: React의 컴포넌트 합성 모델을 따르며, pages/ (라우트 단위) -> components/ (재사용 UI) -> hooks/ (로직 재사용) -> api/ (서버 통신) 계층으로 구분한다. RealWorld 프론트엔드는 9개 라우트의 CRUD UI로 구성되어 있어 Atomic Design이나 FSD 수준의 세분화는 과도하다.

## 4. 모듈 경계 (08-lld-module-spec와 fan-out)

| 모듈 | 역할 | 의존 (fan-out) | 비고 |
|---|---|---|---|
| `backend/controllers` | HTTP 요청 처리 | `services`, `types` | Service만 호출, Prisma 직접 접근 금지 |
| `backend/services` | 비즈니스 로직 | `prisma/client`, `types`, `utils` | Prisma Client 직접 사용 |
| `backend/middlewares` | 요청 전처리 | `utils/jwt`, `prisma/client` | auth, error-handler, validation |
| `backend/routes` | 라우트 정의 | `controllers`, `middlewares` | controller + middleware 조합만 |
| `backend/prisma` | DB 클라이언트 | `@prisma/client` | singleton export |
| `frontend/pages` | 라우트 페이지 | `components`, `hooks`, `api`, `types` | 라우트 당 1 페이지 |
| `frontend/components` | 재사용 UI | `types` | 상태 비의존, props driven |
| `frontend/hooks` | 로직 재사용 | `api`, `types` | 서버 상태 관리 |
| `frontend/api` | 서버 통신 | `types` | fetch wrapper, JWT 주입 |

### fan-out 제약

- Controller -> Service 단방향. Service 간 호출은 허용하되 순환 의존 금지.
- Frontend에서 `api/` 모듈만 외부 HTTP 호출 수행. components/pages에서 직접 fetch 금지.
- `types/`는 모든 모듈에서 import 가능하되, 역방향 의존 금지 (types가 다른 모듈을 import하지 않음).

## 5. 빌드·실행

> **SoT**: 본 절이 빌드/실행 명령의 정본 (ADR-0041). 루트 `LOCAL.md` ss3과 매 PR 동기 갱신.
> **호출 방식**: pnpm native script 직호출 (ADR-0041 -- wrapper 미사용).

### dev (개발)

```bash
# backend (port 3000)
cp backend/.env.dev.example backend/.env
pnpm --filter backend prisma db push
pnpm --filter backend dev

# frontend (port 5173)
cp frontend/.env.dev.example frontend/.env
pnpm --filter frontend dev
```

### stg (스테이징)

```bash
# backend
cp backend/.env.stg.example backend/.env
pnpm --filter backend prisma migrate deploy
pnpm --filter backend build
pnpm --filter backend start:stg

# frontend
cp frontend/.env.stg.example frontend/.env
pnpm --filter frontend build
pnpm --filter frontend preview
```

### prod (프로덕션)

```bash
# backend
cp backend/.env.prod.example backend/.env
pnpm --filter backend prisma migrate deploy
pnpm --filter backend build
pnpm --filter backend start:prod

# frontend
cp frontend/.env.prod.example frontend/.env
pnpm --filter frontend build
pnpm --filter frontend preview
```

### 공통 명령

```bash
# 의존성 설치 (root에서 한 번)
pnpm install

# 전체 빌드
pnpm --filter backend build && pnpm --filter frontend build

# 전체 테스트
pnpm --filter backend test && pnpm --filter frontend test

# 타입 체크
pnpm --filter backend tsc --noEmit && pnpm --filter frontend tsc --noEmit

# lint
pnpm --filter backend lint && pnpm --filter frontend lint
```

## 6. 환경 변수 / 설정 분리

workspace 분리 .env 패턴 (pattern (e)): 각 workspace가 자체 `.env` 파일을 보유한다.

### Backend 환경 변수

| 키 | dev | stg | prod | 노출 위치 |
|---|---|---|---|---|
| `DATABASE_URL` | `file:./dev.db` (SQLite) | `postgresql://user:pass@host:5432/conduit_stg` | `postgresql://user:pass@host:5432/conduit_prod` | `backend/.env` (서버 전용, 클라이언트 미노출) |
| `JWT_SECRET` | `dev-jwt-secret-do-not-use-in-prod` | `(시크릿 관리자에서 주입)` | `(시크릿 관리자에서 주입)` | `backend/.env` (서버 전용, 클라이언트 미노출) |
| `PORT` | `3000` | `3000` | `3000` | `backend/.env` (서버 전용) |
| `NODE_ENV` | `development` | `staging` | `production` | `backend/.env` (서버 전용) |
| `CORS_ORIGIN` | `http://localhost:5173` | `https://stg.conduit.example.com` | `https://conduit.example.com` | `backend/.env` (서버 전용) |

### Frontend 환경 변수

| 키 | dev | stg | prod | 노출 위치 |
|---|---|---|---|---|
| `VITE_API_URL` | `http://localhost:3000/api` | `https://api-stg.conduit.example.com/api` | `https://api.conduit.example.com/api` | `frontend/.env` (Vite 빌드 시 번들에 포함, 클라이언트 노출) |

### 보안 주의

- `JWT_SECRET`은 `.env.*.example` 파일에 placeholder 값만 기재. 실제 값은 커밋 금지.
- `DATABASE_URL`에 실제 DB 비밀번호 기재 금지 -- example 파일에는 더미 값만 사용.
- `VITE_` 접두어 변수는 클라이언트 번들에 포함되므로 시크릿 저장 금지.

## 7. 부팅 자산 (Runnability Assets)

| 자산 | 경로 (profile별) | 변경 trigger 이슈 유형 | 갱신 책임 |
|---|---|---|---|
| 환경 변수 템플릿 (.env.dev.example) | `backend/.env.dev.example`, `frontend/.env.dev.example` | 환경 변수 추가/변경 | 해당 이슈 담당 developer |
| 환경 변수 템플릿 (.env.stg.example) | `backend/.env.stg.example`, `frontend/.env.stg.example` | 환경 변수 추가/변경 | 해당 이슈 담당 developer |
| 환경 변수 템플릿 (.env.prod.example) | `backend/.env.prod.example`, `frontend/.env.prod.example` | 환경 변수 추가/변경 | 해당 이슈 담당 developer |
| 스키마 적용 (dev iteration) | `backend/prisma/schema.prisma` -- `prisma db push` | DB 스키마 변경 | developer (모델 변경 시 동시 반영) |
| DB migrations (stg/prod release) | `backend/prisma/migrations/` -- `prisma migrate deploy` | DB 스키마 변경 (릴리스용) | developer (migration 생성 후 커밋) |
| lockfile (pnpm-lock.yaml) | 루트 `pnpm-lock.yaml` | 의존성 추가/변경/삭제 | `pnpm install` 실행자 (자동 갱신) |
| 설치/seed scripts | `pnpm install` (루트), `pnpm --filter backend prisma db push` (dev), `pnpm --filter backend prisma db seed` (선택) | 초기 세팅 변경 | developer |
| 부팅 명령 (dev) | `pnpm --filter backend dev` + `pnpm --filter frontend dev` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| 부팅 명령 (stg) | `pnpm --filter backend start:stg` + `pnpm --filter frontend preview` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| 부팅 명령 (prod) | `pnpm --filter backend start:prod` + `pnpm --filter frontend preview` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| LOCAL.md | 루트 `LOCAL.md` | 부팅 절차/자산 변경 시 | developer (ADR-0040 -- 같은 PR에서 동기 갱신) |

### DB 스키마 전략 -- (a) 분리형

- **dev iteration**: `prisma db push` -- schema.prisma를 직접 DB에 반영. migration 파일 없이 빠른 iteration.
- **stg/prod release**: `prisma migrate deploy` -- prisma/migrations/ 디렉토리의 정식 migration 파일 적용. `prisma migrate dev`로 migration 생성 후 커밋.

## 8. 스타일링 솔루션

### 솔루션: CSS Modules + Bootstrap 4 CDN

| 항목 | 내용 |
|---|---|
| **솔루션** | CSS Modules |
| **이유** | RealWorld 데모 프론트엔드는 Bootstrap 4 기반 디자인을 사용한다. 글로벌 레이아웃/테마는 Bootstrap CDN으로 처리하고, 컴포넌트 단위 스타일 커스터마이징은 CSS Modules로 scoping하여 클래스명 충돌을 방지한다. 별도 CSS-in-JS 런타임 없이 빌드 타임에 처리되어 번들 사이즈에 유리하다. |
| **의존성** | Vite 내장 CSS Modules 지원 (추가 devDependencies 없음). Bootstrap 4는 CDN `<link>` 태그로 로드 (npm 패키지 불필요). |
| **entrypoint 적용** | `frontend/src/main.tsx`에서 `import "./styles/global.css"` -- global.css에서 Bootstrap CDN override 및 커스텀 변수 정의. `index.html`의 `<head>`에 Bootstrap 4 CDN stylesheet `<link>` 삽입. |
| **디자인 토큰 매핑 (10-lld-screen-design ss3)** | 아래 표 참조 |

### 디자인 토큰 -> CSS Modules 매핑

| 토큰 카테고리 | 토큰 예시 | CSS Modules 위치 | 비고 |
|---|---|---|---|
| Color | `--color-primary: #5CB85C` (Conduit green) | `frontend/src/styles/global.css` `:root` CSS custom properties | Bootstrap 4 기본 색상 override |
| Typography | `--font-family-base: "Source Sans Pro"`, `--font-size-base: 1rem` | `frontend/src/styles/global.css` `:root` | Bootstrap 4 font stack 재정의 |
| Spacing | `--spacing-sm: 0.5rem`, `--spacing-md: 1rem`, `--spacing-lg: 2rem` | `frontend/src/styles/global.css` `:root` | Bootstrap spacing utility와 병행 |
| Component primitives | `.btn-primary`, `.card`, `.nav`, `.tag-pill` | Bootstrap 4 CDN 기본 + `*.module.css` override | 컴포넌트별 `.module.css`에서 Bootstrap 클래스 확장/재정의 |
