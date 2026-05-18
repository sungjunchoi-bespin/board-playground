---
doc_type: coding-conventions
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

# Conduit (RealWorld) — Coding Conventions

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- TypeScript 풀스택 pnpm monorepo 코딩 컨벤션 |

## 1. 명명 규칙

| 항목 | 규칙 | 예 |
|---|---|---|
| 변수 | camelCase | `articleCount`, `currentUser` |
| 함수 | camelCase | `getArticles`, `handleSubmit` |
| React 컴포넌트 | PascalCase | `ArticleList`, `LoginPage` |
| 타입 / 인터페이스 | PascalCase | `ArticleDto`, `UserProfile` |
| 상수 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE`, `JWT_EXPIRATION` |
| 파일명 | kebab-case | `article-list.tsx`, `auth-middleware.ts` |
| 디렉토리명 | kebab-case | `components/`, `middlewares/` |
| CSS Module 파일 | kebab-case + `.module.css` | `article-card.module.css` |
| 환경 변수 | UPPER_SNAKE_CASE (VITE_ prefix for FE) | `DATABASE_URL`, `VITE_API_URL` |
| DB 테이블 (Prisma model) | PascalCase (singular) | `User`, `Article`, `Comment` |
| DB 컬럼 (Prisma field) | camelCase | `createdAt`, `favoritesCount` |
| API 경로 | kebab-case (복수 명사) | `/api/articles`, `/api/tags` |
| enum 값 | UPPER_SNAKE_CASE | `AUTH_INVALID_TOKEN` |

### 보충 규칙

- Boolean 변수/프로퍼티는 `is`, `has`, `can` 접두어를 사용한다: `isAuthenticated`, `hasFollowed`.
- 이벤트 핸들러 prop은 `on` 접두어, 내부 핸들러 함수는 `handle` 접두어를 사용한다: `onClick` (prop) / `handleClick` (handler).
- 제네릭 타입 파라미터는 `T`, `K`, `V` 또는 의미 있는 이름(`TResponse`, `TError`)을 사용한다.

## 2. 에러 코드 PREFIX/SUFFIX

모든 비즈니스 에러는 `{도메인}_{상세}` 형식의 에러 코드를 사용한다. HTTP 상태 코드와 별도로 클라이언트가 에러 유형을 판별할 수 있도록 한다.

| 도메인 | PREFIX | 예 |
|---|---|---|
| 인증 | `AUTH_` | `AUTH_INVALID_CREDENTIALS`, `AUTH_TOKEN_EXPIRED`, `AUTH_UNAUTHORIZED` |
| 아티클 | `ARTICLE_` | `ARTICLE_NOT_FOUND`, `ARTICLE_SLUG_DUPLICATE`, `ARTICLE_FORBIDDEN` |
| 댓글 | `COMMENT_` | `COMMENT_NOT_FOUND`, `COMMENT_FORBIDDEN` |
| 프로필 | `PROFILE_` | `PROFILE_NOT_FOUND`, `PROFILE_SELF_FOLLOW` |
| 태그 | `TAG_` | `TAG_NOT_FOUND` |
| 유효성 검증 | `VALIDATION_` | `VALIDATION_REQUIRED_FIELD`, `VALIDATION_EMAIL_FORMAT`, `VALIDATION_PASSWORD_LENGTH` |

### 에러 응답 형식

RealWorld 스펙에 따라 에러 응답은 다음 형식을 따른다:

```json
{
  "errors": {
    "body": ["error message 1", "error message 2"]
  }
}
```

내부적으로 에러 코드는 서비스 레이어에서 throw하고, 에러 미들웨어에서 위 형식으로 변환한다.

## 3. 언어 관용구

### TypeScript 공통

- **strict mode 필수**: `tsconfig.json`에서 `"strict": true` 활성화.
- **any 사용 금지**: `unknown` 또는 명시적 타입으로 대체한다. 불가피한 경우 `// eslint-disable-next-line @typescript-eslint/no-explicit-any` 주석과 함께 사유를 기록한다.
- **타입 단언 최소화**: `as` 타입 단언 대신 타입 가드(`is`, `in`, `typeof`, `instanceof`)를 사용한다.
- **optional chaining / nullish coalescing**: `?.`, `??` 연산자를 적극 활용하여 null 안전성을 확보한다.
- **enum 대신 const 객체**: `as const` satisfies 패턴을 우선 사용한다. 이유: tree-shaking 유리, 타입 추론 명확.

```typescript
const HttpStatus = {
  OK: 200,
  CREATED: 201,
  NOT_FOUND: 404,
} as const;

type HttpStatus = (typeof HttpStatus)[keyof typeof HttpStatus];
```

### 백엔드 (Express + Prisma)

- **async/await 전용**: 콜백 패턴 사용 금지. express 핸들러는 async wrapper로 감싼다.
- **에러 처리**: 서비스에서 커스텀 에러 클래스를 throw, 글로벌 에러 미들웨어에서 일괄 처리.
- **Prisma 호출**: 서비스 레이어에서만 Prisma Client를 호출한다. 컨트롤러에서 직접 DB 접근 금지.
- **DTO 변환**: 컨트롤러 진입/반환 시 DTO 타입을 명시한다. Prisma 모델을 응답에 직접 노출하지 않는다.

### 프론트엔드 (React + Vite)

- **함수 컴포넌트 전용**: 클래스 컴포넌트 사용 금지.
- **React hooks 규칙 준수**: 커스텀 훅은 `use` 접두어, 조건부 호출 금지.
- **상태 관리**: 로컬 상태는 `useState`/`useReducer`, 서버 상태는 커스텀 API 훅 패턴.
- **이벤트 타입 명시**: `React.ChangeEvent<HTMLInputElement>` 등 이벤트 타입을 명시한다.

## 4. 주석 정책

### 필수 주석

- **파일 헤더**: 불필요 -- 파일명과 디렉토리 구조로 역할을 판단한다.
- **TODO 주석**: `// TODO(담당자): 설명 — 관련 이슈 #N` 형식. 이슈 번호 없는 TODO 금지.
- **FIXME 주석**: `// FIXME(담당자): 설명 — 관련 이슈 #N` 형식. FIXME는 PR 머지 전 해소 권장.

### 권장 주석

- **복잡한 비즈니스 로직**: "왜(Why)" 중심으로 의도를 기록한다. "무엇(What)"은 코드가 설명한다.
- **정규식**: 복잡한 정규식 옆에 의미를 주석으로 설명한다.
- **RealWorld 스펙 참조**: 스펙과 구현이 다를 수 있는 지점에 스펙 URL 주석을 남긴다.

### 금지 주석

- 코드를 그대로 번역한 주석: `// increment count by 1` (코드로 충분).
- 주석 처리된 코드(commented-out code): 삭제하고 git history에 의존한다.

## 5. Lint·포맷

| 도구 | 룰셋 | 자동 강제 |
|---|---|---|
| ESLint | `@typescript-eslint/recommended`, `plugin:react-hooks/recommended` (FE) | pre-commit hook (lint-staged) |
| Prettier | `printWidth: 100`, `singleQuote: true`, `trailingComma: "all"`, `semi: true` | 저장 시 자동 포맷 + pre-commit hook |
| TypeScript | `strict: true`, `noUncheckedIndexedAccess: true` | `tsc --noEmit` (CI/pre-push) |

### 설정 파일 위치

- `eslint.config.js` -- 루트 (monorepo 공통 + workspace override)
- `.prettierrc` -- 루트 (monorepo 공통)
- `tsconfig.json` -- 루트 (base) + workspace별 extends

### lint-staged 설정 (root `package.json`)

```json
{
  "lint-staged": {
    "*.{ts,tsx}": ["eslint --fix", "prettier --write"],
    "*.{json,md,yaml}": ["prettier --write"]
  }
}
```

## 6. Import 정책

### 정렬 순서

1. Node.js 내장 모듈 (`node:fs`, `node:path`)
2. 외부 패키지 (`express`, `react`, `@prisma/client`)
3. workspace 내부 절대 경로 (`@conduit/shared`)
4. 상대 경로 (`./utils`, `../components`)

각 그룹 사이에 빈 줄을 하나 둔다.

### 절대 경로 (workspace alias)

- 백엔드: `tsconfig.json` paths에 `@/*` -> `./src/*` 매핑.
- 프론트엔드: Vite `resolve.alias`에 `@/*` -> `./src/*` 매핑.

```typescript
// Good
import { ArticleService } from "@/services/article-service";
import { ArticleCard } from "@/components/article-card";

// Bad — 깊은 상대 경로
import { ArticleService } from "../../../services/article-service";
```

### named export 우선

- `export default` 대신 `export const`, `export function`, `export type`을 사용한다.
- 예외: React 페이지 컴포넌트는 lazy loading을 위해 `export default` 허용.

### barrel export (index.ts) 정책

- `components/index.ts`, `hooks/index.ts` 등 1 depth barrel만 허용.
- 중첩 barrel (barrel이 barrel을 re-export) 금지 -- 번들 사이즈와 circular dependency 유발.
