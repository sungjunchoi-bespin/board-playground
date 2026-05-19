---
doc_type: coding-conventions
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

# Conduit (RealWorld) — Coding Conventions

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.1 | 2026-05-19 | Agent (developer) | Java 24 + Spring Boot 3.x 백엔드 스택 전환에 따른 전면 재작성 (FE React/TS 규칙 유지) |
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- TypeScript 풀스택 pnpm monorepo 코딩 컨벤션 |

## 1. 명명 규칙

| 항목 | 규칙 | 예 |
|---|---|---|
| 패키지 | lowercase dot-separated | `com.conduit.user.domain.model` |
| 클래스 | PascalCase | `ArticleService`, `CreateArticleUseCase` |
| 인터페이스 | PascalCase (접두어 없음) | `UserRepository`, `RegisterUserUseCase` |
| 메서드 | camelCase | `createArticle`, `findBySlug` |
| 상수 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE`, `JWT_EXPIRATION` |
| DTO | PascalCase + Request/Response suffix | `CreateArticleRequest`, `ArticleResponse` |
| JPA Entity | PascalCase + JpaEntity suffix | `UserJpaEntity`, `ArticleJpaEntity` |
| DB 테이블 | snake_case 복수형 | `users`, `articles`, `article_tags` |
| DB 컬럼 | snake_case | `created_at`, `updated_at` |
| REST endpoint | kebab-case, 복수형 명사 | `/api/articles`, `/api/profiles` |
| React 컴포넌트 (FE) | PascalCase | `ArticleCard`, `Header` |
| React 파일 (FE) | kebab-case | `article-card.tsx`, `use-auth.ts` |
| CSS Module (FE) | camelCase | `styles.articleCard` |

### 보충 규칙

- Boolean 변수/프로퍼티는 `is`, `has`, `can` 접두어를 사용한다: `isAuthenticated`, `hasFollowed`.
- Domain 모델 클래스는 접미어 없이 순수 이름을 사용한다: `User`, `Article`, `Comment`.
- Port 인터페이스는 역할 중심 이름을 사용한다: `LoadUserPort`, `SaveArticlePort`.
- Adapter 클래스는 `*Adapter` 또는 `*Controller` suffix: `UserJpaAdapter`, `ArticleRestController`.
- Record 타입은 DTO, VO에 적극 활용한다: `record CreateArticleRequest(String title, String description, String body, List<String> tagList) {}`.
- Spring Modulith 이벤트 클래스는 `*Event` suffix: `ArticleCreatedEvent`, `UserFollowedEvent`.

## 2. 에러 코드 PREFIX/SUFFIX

모든 비즈니스 에러는 `{도메인}_{상세}` 형식의 에러 코드를 사용한다. HTTP 상태 코드와 별도로 클라이언트가 에러 유형을 판별할 수 있도록 한다.

| 도메인 | PREFIX | 예 |
|---|---|---|
| 인증/인가 | `AUTH_` | `AUTH_INVALID_CREDENTIALS`, `AUTH_TOKEN_EXPIRED` |
| 사용자 | `USER_` | `USER_NOT_FOUND`, `USER_EMAIL_DUPLICATE` |
| 아티클 | `ARTICLE_` | `ARTICLE_NOT_FOUND`, `ARTICLE_SLUG_DUPLICATE` |
| 댓글 | `COMMENT_` | `COMMENT_NOT_FOUND`, `COMMENT_FORBIDDEN` |
| 프로필 | `PROFILE_` | `PROFILE_NOT_FOUND`, `PROFILE_ALREADY_FOLLOWING` |
| 태그 | `TAG_` | `TAG_NOT_FOUND` |
| 입력 검증 | `VALIDATION_` | `VALIDATION_REQUIRED_FIELD`, `VALIDATION_EMAIL_FORMAT` |

### 에러 응답 형식

RealWorld 스펙에 따라 에러 응답은 다음 형식을 따른다:

```json
{
  "errors": {
    "body": ["error message 1", "error message 2"]
  }
}
```

내부적으로 에러 코드는 도메인 예외(`sealed interface`)로 throw하고, `@RestControllerAdvice`의 `@ExceptionHandler`에서 위 형식으로 변환한다. `@Valid` 바인딩 에러(`MethodArgumentNotValidException`)도 같은 형식으로 매핑한다.

### 도메인 예외 구조

```java
public sealed interface ConduitException {
    String errorCode();
    String message();

    record AuthException(String errorCode, String message) implements ConduitException {}
    record ArticleException(String errorCode, String message) implements ConduitException {}
    record UserException(String errorCode, String message) implements ConduitException {}
    record CommentException(String errorCode, String message) implements ConduitException {}
    record ProfileException(String errorCode, String message) implements ConduitException {}
}
```

## 3. 언어 관용구

### Java 24 + Spring Boot 3.x 백엔드

- **Hexagonal 의존 방향 강제**: adapter -> application -> domain. domain 패키지는 외부 프레임워크 의존 금지 (Spring 어노테이션 포함).
- **Spring Modulith 모듈 간 통신**: `ApplicationEventPublisher`를 통한 이벤트 기반 통신. 모듈 간 직접 의존 최소화. `@ApplicationModuleTest`로 모듈 경계 검증.
- **@Transactional 위치**: Application Service(UseCase 구현체) 레벨에서만 선언. Domain 레이어와 Adapter 레이어에서 직접 사용 금지.
- **Record 타입 적극 활용**: DTO(Request/Response), Value Object, 이벤트 클래스에 record 사용. 불변성 보장.
- **sealed interface for domain exceptions**: 도메인 예외는 sealed interface + record 조합으로 정의하여 컴파일 타임에 모든 예외 케이스를 강제한다.
- **JPA Entity 분리**: domain 모델과 JPA Entity를 분리한다. adapter 레이어의 `*JpaEntity`가 `@Entity`를 담당하고, domain 모델은 순수 Java 객체로 유지한다.
- **Optional 반환**: 단건 조회 메서드는 `Optional<T>`를 반환한다. `Optional.get()` 직접 호출 금지, `orElseThrow()`로 도메인 예외를 던진다.
- **null 사용 금지**: 메서드 파라미터와 반환값에 null을 사용하지 않는다. 부재를 표현할 때는 `Optional` 또는 빈 컬렉션을 사용한다.
- **var 사용 허용**: 타입이 명백한 로컬 변수에 한정하여 `var`를 사용한다. 메서드 파라미터, 필드, 반환 타입에는 명시적 타입 선언.

```java
// Good - var 사용이 명백한 경우
var article = articleRepository.findBySlug(slug)
    .orElseThrow(() -> new ArticleException("ARTICLE_NOT_FOUND", "Article not found"));

// Bad - var 사용이 모호한 경우
var result = service.process(data);  // result 타입이 불명확
```

### 프론트엔드 (React 18 + TypeScript + Vite)

- **함수 컴포넌트 전용**: 클래스 컴포넌트 사용 금지.
- **React hooks 규칙 준수**: 커스텀 훅은 `use` 접두어, 조건부 호출 금지.
- **상태 관리**: 로컬 상태는 `useState`/`useReducer`, 서버 상태는 커스텀 API 훅 패턴.
- **이벤트 타입 명시**: `React.ChangeEvent<HTMLInputElement>` 등 이벤트 타입을 명시한다.
- **strict mode 필수**: `tsconfig.json`에서 `"strict": true` 활성화.
- **any 사용 금지**: `unknown` 또는 명시적 타입으로 대체한다.
- **API Base URL**: 백엔드 포트가 8080이므로 Vite 프록시 또는 `VITE_API_URL=http://localhost:8080/api` 환경 변수를 사용한다.

## 4. 주석 정책

### 필수 주석

- **파일 헤더**: 불필요 -- 패키지명과 클래스명으로 역할을 판단한다.
- **TODO 주석**: `// TODO(담당자): 설명 -- 관련 이슈 #N` 형식. 이슈 번호 없는 TODO 금지.
- **FIXME 주석**: `// FIXME(담당자): 설명 -- 관련 이슈 #N` 형식. FIXME는 PR 머지 전 해소 권장.

### 권장 주석

- **복잡한 비즈니스 로직**: "왜(Why)" 중심으로 의도를 기록한다. "무엇(What)"은 코드가 설명한다.
- **Hexagonal 경계**: Port/Adapter 인터페이스에 역할과 의존 방향을 Javadoc으로 명시한다.
- **RealWorld 스펙 참조**: 스펙과 구현이 다를 수 있는 지점에 스펙 URL 주석을 남긴다.
- **JPA 성능 관련**: `@EntityGraph`, fetch join, batch size 등 성능 최적화 결정에 사유 주석.

### 금지 주석

- 코드를 그대로 번역한 주석: `// increment count by 1` (코드로 충분).
- 주석 처리된 코드(commented-out code): 삭제하고 git history에 의존한다.
- 자동 생성 가능한 getter/setter Javadoc: Lombok 또는 record가 대체.

## 5. Lint·포맷

| 도구 | 룰셋 | 자동 강제 |
|---|---|---|
| Checkstyle | Google Java Style Guide | Gradle `checkstyleMain` / `checkstyleTest` 태스크 |
| SpotBugs | Default + FindSecBugs plugin | Gradle `spotbugsMain` 태스크 |
| Spotless (Gradle plugin) | Google Java Format (google-java-format) | `./gradlew spotlessApply` 자동 포맷 + pre-commit hook |
| ESLint (FE) | `@typescript-eslint/recommended`, `plugin:react-hooks/recommended` | pre-commit hook (lint-staged) |
| Prettier (FE) | `printWidth: 100`, `singleQuote: true`, `trailingComma: "all"`, `semi: true` | 저장 시 자동 포맷 + pre-commit hook |
| TypeScript (FE) | `strict: true`, `noUncheckedIndexedAccess: true` | `tsc --noEmit` (CI/pre-push) |

### 백엔드 설정 파일 위치

- `config/checkstyle/checkstyle.xml` -- Google Java Style 기반 커스터마이징
- `build.gradle` -- spotless, checkstyle, spotbugs 플러그인 설정

### Gradle Spotless 설정 예시

```groovy
spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
```

### 프론트엔드 설정 파일 위치

- `frontend/eslint.config.js` -- ESLint 설정
- `frontend/.prettierrc` -- Prettier 설정
- `frontend/tsconfig.json` -- TypeScript strict 설정

### IntelliJ IDEA 설정

- Code Style: Google Java Style 임포트 (Settings > Editor > Code Style > Java > Import Scheme)
- Save Actions: Reformat code + Optimize imports 활성화
- Checkstyle-IDEA 플러그인으로 실시간 검증

## 6. Import 정책

### Java Import 정렬 순서

1. `java.*` (표준 라이브러리)
2. `javax.*` / `jakarta.*` (Jakarta EE)
3. 서드파티 (`org.*`, `com.*`, `io.*`)
4. 프로젝트 내부 (`com.conduit.*`)

각 그룹 사이에 빈 줄을 하나 둔다.

### Java Import 규칙

- **와일드카드 임포트 금지**: `import java.util.*` 대신 `import java.util.List` 등 개별 임포트. Checkstyle에서 강제.
- **static import 허용 범위**: 테스트 코드의 assertion (`assertThat`, `assertEquals` 등)과 `Mockito.*`에 한정.
- **unused import 금지**: Spotless `removeUnusedImports()`가 자동 제거.

### TypeScript Import 정렬 순서 (FE)

1. 외부 패키지 (`react`, `react-router-dom`)
2. 프로젝트 내부 절대 경로 (`@/*`)
3. 상대 경로 (`./utils`, `../components`)

각 그룹 사이에 빈 줄을 하나 둔다.

### 프론트엔드 절대 경로 (alias)

- Vite `resolve.alias`에 `@/*` -> `./src/*` 매핑.

```typescript
// Good
import { ArticleCard } from "@/components/article-card";

// Bad -- 깊은 상대 경로
import { ArticleCard } from "../../../components/article-card";
```

### named export 우선 (FE)

- `export default` 대신 `export const`, `export function`, `export type`을 사용한다.
- 예외: React 페이지 컴포넌트는 lazy loading을 위해 `export default` 허용.
