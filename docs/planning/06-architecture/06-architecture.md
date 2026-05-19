---
doc_type: architecture
gate: C
version: v1.1
date: 2026-05-19
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
| v1.1 | 2026-05-19 | Agent (architect) | BE 스택 전면 교체 -- Express/Prisma/SQLite에서 Spring Boot/JPA/PostgreSQL로 변경. Hexagonal + DDD + Spring Modulith 아키텍처 적용 |
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- 기술 스택 확정, 시스템 컨텍스트/컨테이너 구조 정의 |

## Stack Decision

| 항목 | 결정 | 근거 |
|---|---|---|
| 언어 (BE) | Java 24.x | Virtual Threads, Pattern Matching, Record 등 최신 기능 활용. JVM 생태계 성숙도 |
| 언어 (FE) | TypeScript 5.x | 정적 타입으로 대규모 리팩터링 안전성 확보. React 생태계 표준 |
| 프레임워크 (BE) | Spring Boot 3.x (Jakarta EE) | 성숙 생태계, Spring Security/Modulith 통합, 프로덕션 검증된 인프라 |
| 프레임워크 (FE) | React 18 + Vite 5 | 생태계 최대, RealWorld 레퍼런스 구현체 다수. esbuild 기반 빠른 HMR |
| 아키텍처 | Hexagonal + DDD + Spring Modulith | 도메인 중심 설계, 모듈 경계 명확, 추후 MSA 전환 용이 |
| DB | PostgreSQL | 프로덕션급 RDBMS, JSONB/Full-text search 확장성. 로컬은 Docker 또는 직접 설치 |
| ORM | Spring Data JPA + Hibernate | JPA 표준, Spring Boot 자동 설정, QueryDSL 등 확장 용이 |
| 인증 | Spring Security 6.x + JWT (jjwt) | 필터 체인 기반 인증/인가, RealWorld Token scheme 지원, 선언적 보안 설정 |
| API 문서 | Springdoc OpenAPI 2.x | 코드 기반 자동 Swagger UI 생성, 어노테이션으로 스펙 관리 |
| 빌드 (BE) | Gradle (Kotlin DSL, `build.gradle.kts`) | 빌드 성능, 의존성 관리, incremental build 지원 |
| 빌드 (FE) | pnpm | 디스크 효율(하드링크), lockfile 재현성 우수 |
| 마이그레이션 | Flyway (Spring Boot integration) | 부팅 시 자동 적용, 버전 관리 기반 스키마 진화, SQL native 마이그레이션 |
| 테스트 (BE) | JUnit 5 + MockMvc + Testcontainers + AssertJ | 통합 테스트에 실 PostgreSQL 사용(Testcontainers), 풍부한 assertion |
| 테스트 (FE) | Vitest + Playwright | Vite 생태계 통합, Jest 호환 API, E2E 브라우저 테스트 |
| 스타일링 | CSS Modules + Bootstrap 4 CDN | RealWorld 공식 테마 CSS 활용, 컴포넌트 스코프 스타일링 |

## 1. 시스템 컨텍스트

아래 C4 Level 1 (System Context) 다이어그램은 Conduit 시스템과 외부 액터/시스템의 관계를 보여준다.

```
                    +-----------------+
                    |   사용자 (User)  |
                    |   웹 브라우저     |
                    +--------+--------+
                             |
                             | HTTP (localhost)
                             |
                    +--------v--------+
                    |                 |
                    |  Conduit System |
                    |  (SPA + API +   |
                    |   PostgreSQL)   |
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

> 외부 API 의존 없음. 인증(JWT), 데이터(PostgreSQL) 모두 로컬 자체 처리. 클라우드 서비스 의존 제로.

## 2. 컨테이너 구조

C4 Level 2 (Container) 다이어그램. 멀티스택 모노레포 내 Frontend(pnpm) + Backend(Gradle standalone) + PostgreSQL의 관계를 정의한다.

```
+-----------------------------------------------------------------------+
|  Monorepo (board-playground/)                                         |
|                                                                       |
|  +-----------------------------+   +--------------------------------+ |
|  |  frontend/ (pnpm)           |   |  backend/ (Gradle standalone)  | |
|  |  React 18 SPA               |   |  Spring Boot 3.x               | |
|  |                             |   |                                | |
|  |  - Vite dev server (:5173)  |   |  +----------------------------+| |
|  |  - React Router (hash)      |   |  | Spring Security Filter     || |
|  |  - TypeScript 5.x           |   |  | Chain (JWT 인증/인가)       || |
|  |  - marked (MD render)       |   |  +----------------------------+| |
|  |  - CSS Modules              |   |  | REST Controllers           || |
|  |  - Vitest + Playwright      |   |  | (Inbound Adapters)         || |
|  |                             |   |  +----------------------------+| |
|  |  localhost:5173             |   |  | Application Services       || |
|  +-------------+---------------+   |  | (Use Cases)                || |
|                |                   |  +----------------------------+| |
|                |  HTTP (JSON)      |  | Domain Layer               || |
|                |  /api/*           |  | (Entities, Value Objects,  || |
|                +--------->---------+  |  Ports)                    || |
|                                    |  +----------------------------+| |
|                                    |  | JPA Repositories           || |
|                                    |  | (Outbound Adapters)        || |
|                                    |  +----------------------------+| |
|                                    |  | Flyway Migrations          || |
|                                    |  +----------------------------+| |
|                                    |                                | |
|                                    |  localhost:8080                | |
|                                    +---------------+----------------+ |
|                                                    |                  |
|                                      +-------------v---------------+  |
|                                      |  PostgreSQL                 |  |
|                                      |  (Docker 또는 로컬 설치)     |  |
|                                      |                             |  |
|                                      |  Flyway 마이그레이션 관리    |  |
|                                      |  JDBC 접속                  |  |
|                                      +-----------------------------+  |
+-----------------------------------------------------------------------+
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
| 스타일링 | CSS Modules + Bootstrap 4 CDN |

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
  package.json
```

#### 2.2 Backend (Spring Boot REST API)

| 속성 | 값 |
|---|---|
| 기술 | Java 24 + Spring Boot 3.x + Gradle (Kotlin DSL) |
| 포트 | localhost:8080 |
| API prefix | `/api` |
| 아키텍처 | Hexagonal + DDD + Spring Modulith |
| 인증 | Spring Security 6.x 필터 체인 + JWT (jjwt 라이브러리) |
| ORM | Spring Data JPA + Hibernate |
| 마이그레이션 | Flyway (Spring Boot 자동 적용) |
| API 문서 | Springdoc OpenAPI 2.x (Swagger UI: `/swagger-ui.html`) |
| CORS | WebMvcConfigurer -- FE origin(localhost:5173) 허용 |

**Hexagonal Architecture 레이어 구조**

```
backend/
  src/main/java/com/boardplayground/conduit/
    # -- Spring Modulith 모듈 단위 --
    user/                       # User 도메인 모듈
      adapter/
        in/web/                 # REST Controllers (Inbound Adapter)
        out/persistence/        # JPA Repositories (Outbound Adapter)
      application/              # Application Services (Use Cases)
        port/
          in/                   # Input Ports (인터페이스)
          out/                  # Output Ports (인터페이스)
      domain/                   # Entities, Value Objects
    article/                    # Article 도메인 모듈
      adapter/ ...
      application/ ...
      domain/ ...
    comment/                    # Comment 도메인 모듈
    tag/                        # Tag 도메인 모듈
    global/                     # 공통 설정, 보안, 예외 처리
      config/                   # SecurityConfig, CorsConfig, OpenApiConfig
      security/                 # JwtTokenProvider, JwtAuthenticationFilter
      exception/                # GlobalExceptionHandler
  src/main/resources/
    application.yml             # Spring Boot 설정
    application-dev.yml         # dev profile
    application-stg.yml         # stg profile
    application-prod.yml        # prod profile
    db/migration/               # Flyway SQL 마이그레이션 파일
  src/test/java/                # JUnit 5 + MockMvc + Testcontainers
  build.gradle.kts              # Gradle Kotlin DSL 빌드 스크립트
  settings.gradle.kts
  gradlew                       # Gradle Wrapper
  gradlew.bat
```

#### 2.3 Database (PostgreSQL)

| 속성 | 값 |
|---|---|
| RDBMS | PostgreSQL (로컬 설치 또는 Docker) |
| ORM | Spring Data JPA + Hibernate |
| 마이그레이션 | Flyway -- `db/migration/V{version}__{description}.sql` |
| 접속 | JDBC (`spring.datasource.url`) |
| 테스트 DB | Testcontainers (실 PostgreSQL 컨테이너 자동 기동) |

**JPA 엔티티 (SRS 도메인 모델 기반)**

```
User        -- id, email, username, password, bio, image, createdAt, updatedAt
Article     -- id, slug, title, description, body, author(->User), createdAt, updatedAt
Comment     -- id, body, article(->Article), author(->User), createdAt, updatedAt
Tag         -- id, name (unique)
Follow      -- follower(->User), following(->User) -- 복합 PK
Favorite    -- user(->User), article(->Article) -- 복합 PK
ArticleTag  -- article(->Article), tag(->Tag) -- @ManyToMany join table
```

### 2.4 컨테이너 간 통신

| From | To | 프로토콜 | 설명 |
|---|---|---|---|
| 브라우저 | Frontend (Vite) | HTTP | localhost:5173, SPA 정적 자산 서빙 |
| Frontend | Backend | HTTP (JSON) | `/api/*` 엔드포인트, CORS 허용 |
| Backend | PostgreSQL | JDBC (TCP) | Spring Data JPA가 HikariCP 커넥션 풀 통해 접속 |
| 브라우저 | 외부 CDN | HTTPS | Bootstrap CSS, Google Fonts, Ionicons |

### 2.5 인증 흐름

```
[브라우저]                [Frontend]              [Backend]                [PostgreSQL]
    |                        |                       |                         |
    |-- 로그인 폼 입력 ------>|                       |                         |
    |                        |-- POST /api/users/login -->                     |
    |                        |                       |-- BCrypt 비교 --------->|
    |                        |                       |<-- User row ------------|
    |                        |                       |-- JWT 서명 (jjwt,       |
    |                        |                       |   HS256 + secret key)   |
    |                        |<-- { user: { token }} |                         |
    |                        |-- localStorage.set    |                         |
    |                        |   ("jwtToken", token) |                         |
    |                        |                       |                         |
    |-- 인증 필요 요청 ------>|                       |                         |
    |                        |-- GET /api/articles/feed                        |
    |                        |   Authorization: Token <jwt>                    |
    |                        |                       |                         |
    |                        |               [Spring Security Filter Chain]    |
    |                        |               JwtAuthenticationFilter           |
    |                        |                  -> JWT 검증 + SecurityContext   |
    |                        |                       |                         |
    |                        |                       |-- 팔로잉 피드 조회 ---->|
    |                        |<-- { articles[] }     |                         |
    |<-- 피드 렌더링 ---------|                       |                         |
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
| 환경 변수 분리 | `application-dev.yml` | `application-stg.yml`, `application-prod.yml` profile별 |
| DB | PostgreSQL (로컬/Docker) | 관리형 PostgreSQL (RDS 등) |
| CORS origin | `localhost:5173` | 도메인 URL |
| 포트 설정 | `server.port` 환경 변수 | 클라우드 플랫폼 자동 할당 |
| 정적 자산 | Vite dev server | `vite build` -> CDN/정적 호스팅 |
| API URL | FE에서 상대 경로 또는 env 변수 | 환경별 API base URL |
| 모듈화 | Spring Modulith (모놀리스) | 모듈 분리 -> MSA 전환 가능 |

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
| 브라우저 <-> Backend | Spring Security 필터 체인으로 JWT 검증. `Authorization: Token <jwt>` 헤더 필수 (인증 엔드포인트) |
| Backend <-> PostgreSQL | JDBC 접속. HikariCP 커넥션 풀. 접속 정보는 환경 변수/profile로 관리 |
| 비밀번호 | Spring Security PasswordEncoder (BCrypt) 해싱 저장. 평문 저장/로깅 금지 |
| JWT Secret | 환경 변수(`JWT_SECRET`)로 관리. 코드/커밋에 하드코딩 금지. HS256 알고리즘 |
| XSS | marked 라이브러리의 `sanitize` 옵션 활성화. 사용자 입력 HTML 이스케이프 |

### 시스템 경계 요약

```
+-----------------------------------------------------------+
|  로컬 머신 (localhost)                                     |
|                                                           |
|  [Browser] <-> [Vite:5173] <-> [Spring Boot:8080] <-> [PostgreSQL:5432]
|      |                                                    |
|      +--- CDN (Bootstrap/Fonts/Icons) [외부, 읽기전용]     |
+-----------------------------------------------------------+
```

> 전체 시스템이 단일 로컬 머신 내에서 동작한다. 외부 통신은 CDN 자원 로딩뿐이며, 이마저도 로컬 번들 fallback으로 오프라인 동작이 가능하다. Backend는 Hexagonal Architecture + Spring Modulith로 구성되어 도메인 모듈 간 경계가 명확하며, 추후 MSA 전환 시 모듈 단위로 서비스를 분리할 수 있다. PostgreSQL은 Docker 또는 로컬 설치로 기동하며, Flyway가 부팅 시 스키마 마이그레이션을 자동 적용한다.
