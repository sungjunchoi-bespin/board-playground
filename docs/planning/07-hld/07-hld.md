---
doc_type: hld
gate: C
version: v1.1
date: 2026-05-19
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
| v1.1 | 2026-05-19 | Agent (architect) | BE 스택 전환: Express+Prisma+SQLite -> Spring Boot 3.x+JPA+PostgreSQL, Hexagonal+DDD+Spring Modulith 아키텍처 도입 |
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- FE 7모듈 + BE 10모듈 분해, 데이터 흐름, 비기능 대응 정의 |

## 1. 핵심 모듈 / 컴포넌트

| 모듈 | 책임 | 의존 | 08에서 상세 |
|---|---|---|---|
| App Shell (FE) | 라우팅, 전역 레이아웃, 인증 상태, API 클라이언트 | react-router-dom | 08-M-FE-01 |
| Auth Module (FE) | 회원가입/로그인/사용자 CRUD, JWT localStorage | App Shell | 08-M-FE-02 |
| Article Module (FE) | 아티클 CRUD, 즐겨찾기, 마크다운 렌더링 | App Shell, Auth, Comment, Tag | 08-M-FE-03 |
| Profile Module (FE) | 프로필 조회, 팔로우/언팔로우 | App Shell, Auth, Feed | 08-M-FE-04 |
| Feed Module (FE) | 글로벌/개인 피드, 페이지네이션 | App Shell, Auth, Tag | 08-M-FE-05 |
| Comment Module (FE) | 댓글 CRUD | App Shell, Auth | 08-M-FE-06 |
| Tag Module (FE) | 태그 목록, 필터링 | App Shell | 08-M-FE-07 |
| user (BE) | 회원가입(R-F-01), 로그인(R-F-02), 사용자 조회/수정(R-F-03,04), 비밀번호 해싱 | shared/security, JPA | 08-M-BE-01 |
| article (BE) | 아티클 CRUD(R-F-08~13), 즐겨찾기(R-F-17,18), 피드(R-F-09), slug(R-N-06), 페이지네이션(R-N-05) | user, tag, shared/security, JPA | 08-M-BE-02 |
| comment (BE) | 댓글 CRUD(R-F-14~16), 권한 검증 | article, user, shared/security, JPA | 08-M-BE-03 |
| profile (BE) | 프로필 조회(R-F-05), 팔로우/언팔로우(R-F-06,07) | user, shared/security, JPA | 08-M-BE-04 |
| tag (BE) | 태그 목록(R-F-19) | JPA | 08-M-BE-05 |
| shared/security (BE) | Spring Security 설정, JWT 필터, CORS, 전역 에러 핸들러(@RestControllerAdvice), OpenAPI 설정 | Spring Security, jjwt | 08-M-BE-06 |

> 각 모듈의 상세 설계는 08-lld-module-spec에서 fan-out. 아래는 모듈 맵과 계층별 상세이다.

### 1.1 전체 모듈 맵

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Frontend (React 18 + Vite)                   │
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
│              Backend (Spring Boot 3.x + Spring Modulith)            │
│                                                                      │
│  ┌────────────────── shared/security ──────────────────────────────┐ │
│  │  Spring Security FilterChain │ JwtAuthFilter │ CORS │ ErrorAdv │ │
│  │  Springdoc OpenAPI 설정       │ 전역 예외 핸들러                  │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌─── user ───┐ ┌── article ──┐ ┌── comment ──┐ ┌── profile ──┐   │
│  │ adapter/in │ │ adapter/in  │ │ adapter/in  │ │ adapter/in  │   │
│  │  /web      │ │  /web       │ │  /web       │ │  /web       │   │
│  │ application│ │ application │ │ application │ │ application │   │
│  │  /service  │ │  /service   │ │  /service   │ │  /service   │   │
│  │ domain     │ │ domain      │ │ domain      │ │ domain      │   │
│  │  /model    │ │  /model     │ │  /model     │ │  /model     │   │
│  │  /port     │ │  /port      │ │  /port      │ │  /port      │   │
│  │ adapter/out│ │ adapter/out │ │ adapter/out │ │ adapter/out │   │
│  │  /persist  │ │  /persist   │ │  /persist   │ │  /persist   │   │
│  └────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │
│                                                                      │
│  ┌── tag ─────┐                                                      │
│  │ adapter/in │                                                      │
│  │ application│                                                      │
│  │ domain     │                                                      │
│  │ adapter/out│                                                      │
│  └────────────┘                                                      │
│                                                                      │
│  ┌──────── Spring Data JPA + Hibernate (ORM) ─────────────────────┐ │
│  │  Entity Mapping │ Repository 구현 │ Flyway Migration           │ │
│  └───────────────────────────┬─────────────────────────────────────┘ │
└──────────────────────────────┼──────────────────────────────────────┘
                               │ JDBC
                        ┌──────▼──────┐
                        │ PostgreSQL  │
                        │ (port 5432) │
                        └─────────────┘
```

### 1.2 프론트엔드 모듈 (React 18 + TypeScript + Vite, UNCHANGED)

| 모듈 | 책임 | 의존 | 08에서 상세 |
|---|---|---|---|
| App Shell | 라우팅(react-router, hash mode), 전역 레이아웃(Header/Footer), 인증 상태 전역 관리, API 클라이언트(axios/fetch wrapper) | react-router-dom | 08-M-FE-01 |
| Auth Module | 회원가입(R-F-01), 로그인(R-F-02), 현재 사용자 조회(R-F-03), 사용자 정보 수정(R-F-04), JWT localStorage 관리, 로그아웃 | App Shell (API client, Router) | 08-M-FE-02 |
| Article Module | 아티클 단건 조회(R-F-10), 아티클 생성(R-F-11), 아티클 수정(R-F-12), 아티클 삭제(R-F-13), 마크다운 렌더링, 즐겨찾기 토글(R-F-17, R-F-18) | App Shell, Auth Module, Comment Module, Tag Module | 08-M-FE-03 |
| Profile Module | 프로필 조회(R-F-05), 팔로우(R-F-06), 언팔로우(R-F-07), 사용자 아티클/즐겨찾기 탭 표시 | App Shell, Auth Module, Feed Module | 08-M-FE-04 |
| Feed Module | 글로벌 피드(R-F-08), 개인 피드(R-F-09), 태그 필터 피드, 아티클 카드 목록, 페이지네이션(R-N-05) | App Shell, Auth Module, Tag Module | 08-M-FE-05 |
| Comment Module | 댓글 추가(R-F-14), 댓글 목록 조회(R-F-15), 댓글 삭제(R-F-16) | App Shell, Auth Module | 08-M-FE-06 |
| Tag Module | 태그 목록 조회(R-F-19), 사이드바 태그 클라우드, 태그 클릭 필터링 | App Shell | 08-M-FE-07 |

### 1.3 백엔드 모듈 (Spring Boot 3.x + Spring Modulith + Hexagonal)

각 BE 모듈은 Spring Modulith의 bounded context로 구성되며, 내부는 Hexagonal Architecture를 따른다.

**Hexagonal 레이어 구조 (모듈 공통)**:
- `domain/model` -- Entity, Value Object (JPA @Entity 포함)
- `domain/port/in` -- UseCase 인터페이스 (인바운드 포트)
- `domain/port/out` -- Repository 포트 인터페이스 (아웃바운드 포트)
- `application/service` -- UseCase 구현체 (@Service, @Transactional)
- `adapter/in/web` -- REST 엔드포인트 (@RestController)
- `adapter/out/persistence` -- JPA Repository 구현 (@Repository, Spring Data JPA)

| 모듈 | 책임 | 의존 | 08에서 상세 |
|---|---|---|---|
| user | 회원가입 POST /api/users(R-F-01), 로그인 POST /api/users/login(R-F-02), 현재 사용자 GET /api/user(R-F-03), 사용자 수정 PUT /api/user(R-F-04), 비밀번호 해싱(BCryptPasswordEncoder), JWT 발급 | shared/security, Spring Data JPA | 08-M-BE-01 |
| article | 아티클 목록 GET /api/articles(R-F-08), 피드 GET /api/articles/feed(R-F-09), 단건 GET /api/articles/:slug(R-F-10), 생성 POST /api/articles(R-F-11), 수정 PUT /api/articles/:slug(R-F-12), 삭제 DELETE /api/articles/:slug(R-F-13), 즐겨찾기 POST/DELETE /api/articles/:slug/favorite(R-F-17, R-F-18), slug 생성(R-N-06), 페이지네이션(R-N-05) | user, tag, shared/security, Spring Data JPA | 08-M-BE-02 |
| comment | 댓글 추가 POST /api/articles/:slug/comments(R-F-14), 목록 GET /api/articles/:slug/comments(R-F-15), 삭제 DELETE /api/articles/:slug/comments/:id(R-F-16), 권한 검증(자기 댓글만 삭제) | article, user, shared/security, Spring Data JPA | 08-M-BE-03 |
| profile | 프로필 조회 GET /api/profiles/:username(R-F-05), 팔로우 POST /api/profiles/:username/follow(R-F-06), 언팔로우 DELETE /api/profiles/:username/follow(R-F-07) | user, shared/security, Spring Data JPA | 08-M-BE-04 |
| tag | 태그 목록 GET /api/tags(R-F-19), 아티클-태그 연결 관리 | Spring Data JPA | 08-M-BE-05 |
| shared/security | Spring Security 6.x FilterChain 설정(SecurityFilterChain @Bean), JWT 인증 필터(OncePerRequestFilter), CORS 설정(CorsConfigurationSource), 전역 에러 핸들러(@RestControllerAdvice), Springdoc OpenAPI 설정 | Spring Security 6.x, jjwt, springdoc-openapi | 08-M-BE-06 |

### 1.4 모듈 간 의존 관계

```
shared/security  ← (모든 BE 모듈이 의존)
     │
     ├── user       ← profile, article, comment
     ├── tag        ← article
     ├── article    ← comment
     ├── profile    (user에만 의존)
     └── comment    (article, user에 의존)
```

Spring Modulith는 모듈 간 순환 의존을 컴파일 타임에 감지하여 차단한다. 모듈 간 통신은 Spring Modulith의 `@ApplicationModuleTest`로 격리 테스트 가능하다.

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
[Spring Security FilterChain]
   │  CorsFilter → (JwtAuthFilter skip: 인증 불필요 경로)
   ▼
[UserController (adapter/in/web)]
   │  @PostMapping("/api/users"), @PostMapping("/api/users/login")
   ▼
[RegisterUserUseCase / LoginUserUseCase (domain/port/in)]
   │
   ▼
[UserService (application/service)]
   ├─ password 해싱/검증 (BCryptPasswordEncoder)
   ├─ email/username 중복 검사 (UserRepository port)
   └─ JWT 발급 (JwtProvider from shared/security)
   │
   ▼
[UserJpaRepository (adapter/out/persistence)]
   │  Spring Data JPA → Hibernate
   ▼
[PostgreSQL]
   │
   (UserEntity record)
   │
   ▼
[UserController]
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
[Spring Security FilterChain]
   │  CorsFilter → JwtAuthFilter
   │     ├─ Authorization 헤더 파싱 (Token scheme)
   │     ├─ jjwt로 JWT 검증/디코딩
   │     └─ SecurityContextHolder에 Authentication 주입
   ▼
[ArticleController (adapter/in/web)]
   │  @PostMapping("/api/articles")
   │  @AuthenticationPrincipal로 현재 사용자 주입
   ▼
[CreateArticleUseCase (domain/port/in)]
   │
   ▼
[ArticleService (application/service)]
   ├─ slug 생성 (title → Slugify 라이브러리, R-N-06)
   ├─ 태그 upsert (TagRepository port 위임)
   └─ 권한 검증 (수정/삭제: 작성자만)
   │
   ▼
[ArticleJpaRepository (adapter/out/persistence)]
   │  Spring Data JPA → Hibernate
   ├─ Article CREATE/UPDATE/DELETE
   ├─ ArticleTag 연결 (JoinTable)
   └─ 관련 Comment cascade 삭제
   │
   ▼
[PostgreSQL]
```

### 2.3 피드/목록 조회 흐름

```
[Feed Module (FE)]
   │  GET /api/articles?tag=X&author=Y&favorited=Z&limit=20&offset=0
   │  또는 GET /api/articles/feed (인증 필수)
   ▼
[Spring Security FilterChain]
   │  CorsFilter → JwtAuthFilter (optional/required)
   ▼
[ArticleController (adapter/in/web)]
   │  @GetMapping("/api/articles"), @GetMapping("/api/articles/feed")
   ▼
[ListArticlesUseCase / GetFeedUseCase (domain/port/in)]
   │
   ▼
[ArticleService (application/service)]
   ├─ 필터 조건 조합 (JPA Specification 또는 QueryDSL)
   ├─ 페이지네이션 (Pageable → limit/offset 변환, R-N-05)
   ├─ feed: 팔로잉 사용자 ID 목록 조회 (profile 모듈 연동)
   └─ favorited/following 상태 계산
   │
   ▼
[ArticleJpaRepository (adapter/out/persistence)]
   │  Spring Data JPA → Hibernate → PostgreSQL
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
[Spring Security FilterChain]
   │  CorsFilter → JwtAuthFilter
   ▼
[CommentController (adapter/in/web)]
   │  @PostMapping("/api/articles/{slug}/comments")
   ▼
[AddCommentUseCase (domain/port/in)]
   │
   ▼
[CommentService (application/service)]
   ├─ 아티클 존재 여부 검증 (slug → ArticleRepository port)
   ├─ 댓글 생성
   └─ 삭제 시 권한 검증 (작성자만)
   │
   ▼
[CommentJpaRepository (adapter/out/persistence)]
   │  Spring Data JPA → Hibernate → PostgreSQL
```

### 2.5 프로필/팔로우 흐름

```
[Profile Module (FE)]
   │  GET /api/profiles/:username
   │  POST /api/profiles/:username/follow
   │  DELETE /api/profiles/:username/follow
   ▼
[Spring Security FilterChain]
   │  CorsFilter → JwtAuthFilter (optional/required)
   ▼
[ProfileController (adapter/in/web)]
   │  @GetMapping("/api/profiles/{username}")
   │  @PostMapping("/api/profiles/{username}/follow")
   │  @DeleteMapping("/api/profiles/{username}/follow")
   ▼
[GetProfileUseCase / FollowUserUseCase (domain/port/in)]
   │
   ▼
[ProfileService (application/service)]
   ├─ 사용자 조회 (username → UserRepository port)
   ├─ following 상태 계산
   └─ Follow 레코드 CREATE/DELETE
   │
   ▼
[FollowJpaRepository (adapter/out/persistence)]
   │  Spring Data JPA → Hibernate → PostgreSQL
```

### 2.6 에러 흐름 (횡단 관심사 -- @RestControllerAdvice)

```
[Any Controller/Service]
   │  throw CustomException (extends RuntimeException)
   │  예: UnauthorizedException, ForbiddenException, NotFoundException, ValidationException
   ▼
[@RestControllerAdvice GlobalExceptionHandler (shared/security)]
   │  @ExceptionHandler 메서드로 예외 타입별 분기
   ├─ 422 ValidationException  → { "errors": { "field": ["message"] } }
   ├─ 401 UnauthorizedException → { "errors": { "auth": ["unauthorized"] } }
   ├─ 403 ForbiddenException    → { "errors": { "auth": ["forbidden"] } }
   ├─ 404 NotFoundException     → { "errors": { "resource": ["not found"] } }
   ├─ DataIntegrityViolationException → 422 중복 위반 매핑
   └─ 500 Exception (fallback)  → { "errors": { "server": ["internal error"] } }
   ▼
[Response] Content-Type: application/json (R-N-01, Spring Boot 기본)
```

## 3. 비기능 대응

| 비기능 R-ID | 대응 전략 | 상세 |
|---|---|---|
| R-N-01 (API 응답 형식) | Spring Boot 기본 + @RestController | @RestController가 모든 핸들러 반환값을 자동으로 JSON 직렬화(Jackson). Content-Type: application/json이 기본 적용. 별도 미들웨어 불필요. 통합 테스트에서 MockMvc로 Content-Type 검증 |
| R-N-02 (에러 응답 형식) | @RestControllerAdvice + 커스텀 예외 클래스 계층 | CustomException(HttpStatus, Map<String, List<String>>) 추상 클래스를 정의하고 각 예외(Unauthorized, Forbidden, NotFound, Validation)가 상속. GlobalExceptionHandler가 @ExceptionHandler로 RealWorld 에러 포맷 { "errors": { "field": ["message"] } } 변환. Spring Data JPA의 DataIntegrityViolationException도 422로 매핑 |
| R-N-03 (JWT 인증) | Spring Security 6.x OncePerRequestFilter + jjwt | JwtAuthFilter extends OncePerRequestFilter: Authorization 헤더에서 `Token <jwt>` 파싱(Bearer가 아닌 Token scheme). jjwt(io.jsonwebtoken)로 HS256 서명 검증 후 UsernamePasswordAuthenticationToken 생성하여 SecurityContextHolder에 주입. 필수 인증(authenticated)과 선택 인증(permitAll + SecurityContext 있으면 사용) 두 모드 지원. JWT secret은 환경변수(JWT_SECRET)로 관리, application.yml의 ${JWT_SECRET}으로 주입. 만료 시간은 설정 가능 (기본 7d) |
| R-N-04 (CORS) | Spring Security CorsConfigurationSource + WebMvcConfigurer | SecurityFilterChain 내 `.cors(c -> c.configurationSource(corsConfigurationSource()))` 설정. CorsConfiguration에서 allowedOrigins(FE localhost:5173), allowedMethods(GET,POST,PUT,DELETE,OPTIONS), allowedHeaders(Authorization, Content-Type) 지정. preflight OPTIONS 자동 처리. 환경별 origin은 application-{profile}.yml로 분리 관리 |
| R-N-05 (페이지네이션) | Spring Data JPA Pageable + limit/offset 변환 | RealWorld 스펙의 limit/offset 쿼리 파라미터를 커스텀 ArgumentResolver 또는 서비스 레이어에서 PageRequest.of(offset/limit, limit)로 변환. limit 기본 20, 최대 100. 응답에 articlesCount(Page.getTotalElements()) 포함으로 클라이언트 페이지네이션 UI 지원 |
| R-N-06 (Slug 자동 생성) | Slugify 라이브러리(com.github.slugify) + unique 제약 | title을 lowercase-hyphenated 형식으로 변환 (예: "How to Train" -> "how-to-train"). 중복 slug 발생 시 suffix 추가 (UUID 앞 8자리 접미사). Article 생성/수정(title 변경) 시 모두 적용. PostgreSQL unique 제약(@Column(unique=true)) + Flyway 마이그레이션으로 DB 레벨 중복 방지 |

## 4. 외부 인터페이스 윤곽

### 4.1 FE-BE 인터페이스

- **프로토콜**: HTTP/1.1 (로컬 실행)
- **Base URL**: `http://localhost:8080/api` (Spring Boot 기본 포트 8080)
- **Content-Type**: `application/json` (Spring Boot + Jackson 기본)
- **인증 헤더**: `Authorization: Token <jwt>` (RealWorld 스펙 -- Bearer가 아닌 Token scheme)
- **API 문서**: `http://localhost:8080/swagger-ui.html` (Springdoc OpenAPI 2.x)
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
| PostgreSQL | 데이터 영속화 | Spring Data JPA + Hibernate ORM 추상화. 접속 정보는 환경변수(SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD)로 관리. application-{profile}.yml로 환경별 분리 |
| Flyway | DB 스키마 마이그레이션 | Spring Boot 자동 설정. src/main/resources/db/migration/ 하위 V{n}__{desc}.sql 파일로 버전 관리. 애플리케이션 시작 시 자동 적용 |

### 4.4 프로젝트 구조 (윤곽)

```
conduit/
  ├── package.json              # pnpm 워크스페이스 루트
  ├── pnpm-workspace.yaml       # packages: ["packages/*"]
  ├── packages/
  │   └── frontend/             # React 18 + TypeScript + Vite SPA
  │       ├── package.json
  │       ├── vite.config.ts
  │       └── src/
  ├── backend/                  # Spring Boot 3.x (Gradle 프로젝트)
  │   ├── build.gradle.kts      # Gradle Kotlin DSL
  │   ├── settings.gradle.kts
  │   ├── gradlew, gradlew.bat
  │   └── src/
  │       ├── main/
  │       │   ├── java/com/conduit/
  │       │   │   ├── user/                # Spring Modulith 모듈
  │       │   │   │   ├── domain/model/
  │       │   │   │   ├── domain/port/in/
  │       │   │   │   ├── domain/port/out/
  │       │   │   │   ├── application/service/
  │       │   │   │   ├── adapter/in/web/
  │       │   │   │   └── adapter/out/persistence/
  │       │   │   ├── article/             # 동일 Hexagonal 구조
  │       │   │   ├── comment/
  │       │   │   ├── profile/
  │       │   │   ├── tag/
  │       │   │   └── shared/security/     # 공유 보안 설정
  │       │   └── resources/
  │       │       ├── application.yml
  │       │       ├── application-dev.yml
  │       │       ├── application-stg.yml
  │       │       ├── application-prod.yml
  │       │       └── db/migration/        # Flyway 마이그레이션
  │       └── test/
  ├── .env.dev.example          # FE 환경변수 템플릿
  ├── .env.stg.example
  └── .env.prod.example
```
