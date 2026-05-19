---
doc_type: scaffolding
gate: C
version: v1.1
date: 2026-05-19
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# Conduit (RealWorld) Frontend — Scaffolding

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.1 | 2026-05-19 | Agent (architect) | FE-only로 축소 -- BE가 Java/Spring Boot로 전환됨. backend/ 트리 제거, pnpm workspace 해제, FE 전용 빌드/환경 변수/부팅 자산으로 재작성 |
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- TypeScript pnpm monorepo scaffolding (FE: React+Vite, BE: Express+Prisma) |

## 1. 디렉토리 트리

React + Vite 기반 frontend 단독 프로젝트. `frontend/` 디렉토리에 독립 배치. pnpm workspace 없이 단일 패키지로 운영.

```
frontend/
├── src/
│   ├── components/                   # 재사용 UI 컴포넌트
│   │   ├── article-card.tsx
│   │   ├── article-card.module.css
│   │   ├── header.tsx
│   │   └── ...
│   ├── pages/                        # 라우트 단위 페이지 컴포넌트
│   │   ├── home-page.tsx
│   │   ├── login-page.tsx
│   │   ├── register-page.tsx
│   │   ├── article-page.tsx
│   │   ├── editor-page.tsx
│   │   ├── settings-page.tsx
│   │   ├── profile-page.tsx
│   │   └── ...
│   ├── hooks/                        # 커스텀 React 훅
│   │   ├── use-auth.ts
│   │   ├── use-articles.ts
│   │   └── ...
│   ├── api/                          # API 호출 함수 (fetch wrapper)
│   │   ├── client.ts                 # base fetch wrapper (JWT 주입)
│   │   ├── auth-api.ts
│   │   ├── article-api.ts
│   │   └── ...
│   ├── types/                        # 공유 타입 정의
│   │   ├── article.ts
│   │   ├── user.ts
│   │   └── ...
│   ├── styles/                       # 글로벌 스타일, 디자인 토큰
│   │   └── global.css
│   ├── app.tsx                       # 라우터 + 레이아웃
│   └── main.tsx                      # entrypoint (stylesheet import)
├── public/
├── index.html
├── .env.dev.example
├── .env.stg.example
├── .env.prod.example
├── package.json
├── tsconfig.json
├── vite.config.ts
└── pnpm-lock.yaml
```

## 2. 패키지 명명 규칙

| 범위 | 규칙 | 예 |
|---|---|---|
| frontend package | `conduit-frontend` | `frontend/package.json` > `"name": "conduit-frontend"` |
| 컴포넌트 파일 | kebab-case | `article-card.tsx`, `header.tsx` |
| 페이지 파일 | kebab-case + `-page` 접미어 | `home-page.tsx`, `login-page.tsx` |
| 훅 파일 | `use-` 접두어 + kebab-case | `use-auth.ts`, `use-articles.ts` |
| API 파일 | kebab-case + `-api` 접미어 | `auth-api.ts`, `article-api.ts` |
| 타입 파일 | kebab-case (모델명) | `article.ts`, `user.ts` |
| CSS Modules | `*.module.css` | `article-card.module.css` |

- pnpm workspace를 사용하지 않으므로 `@conduit/` scope 불필요.
- `frontend/` 디렉토리 내에서 `pnpm install`, `pnpm dev` 등 직접 실행.

## 3. 디자인 패턴 결정

### Frontend -- Component-based Architecture

**선택 패턴**: Layered (Component-based)

**이유**: React의 컴포넌트 합성 모델을 따르며, pages/ (라우트 단위) -> components/ (재사용 UI) -> hooks/ (로직 재사용) -> api/ (서버 통신) 계층으로 구분한다. RealWorld 프론트엔드는 9개 라우트의 CRUD UI로 구성되어 있어 Atomic Design이나 FSD 수준의 세분화는 과도하다.

## 4. 모듈 경계 (08-lld-module-spec와 fan-out)

| 모듈 | 역할 | 의존 (fan-out) | 비고 |
|---|---|---|---|
| `pages/` | 라우트 페이지 | `components`, `hooks`, `api`, `types` | 라우트 당 1 페이지 |
| `components/` | 재사용 UI | `types` | 상태 비의존, props driven |
| `hooks/` | 로직 재사용 | `api`, `types` | 서버 상태 관리 |
| `api/` | 서버 통신 | `types` | fetch wrapper, JWT 주입 |
| `types/` | 타입 정의 | 없음 | 순수 TypeScript 타입 |
| `styles/` | 글로벌 스타일 | 없음 | CSS custom properties, Bootstrap override |

### fan-out 제약

- `api/` 모듈만 외부 HTTP 호출 수행. components/pages에서 직접 fetch 금지.
- `types/`는 모든 모듈에서 import 가능하되, 역방향 의존 금지 (types가 다른 모듈을 import하지 않음).
- components는 props를 통해서만 데이터를 수신. 내부에서 hooks를 직접 호출하지 않는 것이 원칙 (pages에서 hooks 호출 후 props로 전달).

## 5. 빌드·실행

> **SoT**: 본 절이 frontend 빌드/실행 명령의 정본 (ADR-0041). 루트 `LOCAL.md` ss3과 매 PR 동기 갱신.
> **호출 방식**: pnpm native script 직호출 (ADR-0041 -- wrapper 미사용).

| 명령 | 설명 |
|---|---|
| `pnpm dev` | dev 서버 부팅 (port 5173) |
| `pnpm build` | 프로덕션 빌드 |
| `pnpm preview` | 빌드 결과 로컬 프리뷰 |
| `pnpm test` | 테스트 실행 |
| `pnpm tsc --noEmit` | 타입 체크 |
| `pnpm lint` | ESLint 실행 |

### dev (개발)

```bash
cd frontend
cp .env.dev.example .env
pnpm install
pnpm dev
# port 5173, API -> http://localhost:8080/api (backend dev)
```

### stg (스테이징)

```bash
cd frontend
cp .env.stg.example .env
pnpm install
pnpm build
pnpm preview
# port 4173 (Vite preview default), API -> https://api-stg.conduit.example.com/api
```

### prod (프로덕션)

```bash
cd frontend
cp .env.prod.example .env
pnpm install
pnpm build
pnpm preview
# port 4173, API -> https://api.conduit.example.com/api
# 실제 배포 시 build/ 산출물을 CDN/정적 호스팅에 업로드
```

### 공통 명령

```bash
# 의존성 설치
cd frontend
pnpm install

# 프로덕션 빌드
pnpm build

# 타입 체크
pnpm tsc --noEmit

# lint
pnpm lint

# 테스트
pnpm test
```

## 6. 환경 변수 / 설정 분리

frontend `.env` 파일 기반 분리. Vite의 `VITE_` 접두어 환경 변수만 클라이언트 번들에 포함.

| 키 | dev | stg | prod | 노출 위치 |
|---|---|---|---|---|
| `VITE_API_URL` | `http://localhost:8080/api` | `https://api-stg.conduit.example.com/api` | `https://api.conduit.example.com/api` | `frontend/.env` (Vite 빌드 시 번들에 포함, 클라이언트 노출) |

### 보안 주의

- `VITE_` 접두어 변수는 클라이언트 번들에 포함되므로 시크릿 저장 금지.
- API URL은 공개 정보이므로 `.env.*.example` 파일에 실제 값 기재 가능.
- 인증 토큰(JWT)은 환경 변수가 아닌 런타임에 localStorage/memory에서 관리.

## 7. 부팅 자산 (Runnability Assets)

| 자산 | 경로 (profile별) | 변경 trigger 이슈 유형 | 갱신 책임 |
|---|---|---|---|
| 환경 변수 템플릿 (.env.dev.example) | `frontend/.env.dev.example` | 환경 변수 추가/변경 | 해당 이슈 담당 developer |
| 환경 변수 템플릿 (.env.stg.example) | `frontend/.env.stg.example` | 환경 변수 추가/변경 | 해당 이슈 담당 developer |
| 환경 변수 템플릿 (.env.prod.example) | `frontend/.env.prod.example` | 환경 변수 추가/변경 | 해당 이슈 담당 developer |
| 스키마 적용 (dev iteration) | N/A -- FE-only, DB 없음 | N/A | N/A |
| DB migrations (stg/prod release) | N/A -- FE-only, DB 없음 | N/A | N/A |
| lockfile (pnpm-lock.yaml) | `frontend/pnpm-lock.yaml` | 의존성 추가/변경/삭제 | `pnpm install` 실행자 (자동 갱신) |
| 설치/seed scripts | `cd frontend && pnpm install` | 초기 세팅 변경 | developer |
| 부팅 명령 (dev) | `cd frontend && cp .env.dev.example .env && pnpm install && pnpm dev` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| 부팅 명령 (stg) | `cd frontend && cp .env.stg.example .env && pnpm install && pnpm build && pnpm preview` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| 부팅 명령 (prod) | `cd frontend && cp .env.prod.example .env && pnpm install && pnpm build && pnpm preview` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| LOCAL.md | 루트 `LOCAL.md` | 부팅 절차/자산 변경 시 | developer (ADR-0040 -- 같은 PR에서 동기 갱신) |

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
