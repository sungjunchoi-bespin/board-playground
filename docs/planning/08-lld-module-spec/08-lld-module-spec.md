---
doc_type: module-spec
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

# Conduit (RealWorld) — Module Spec (LLD — 모듈/통신)

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.1 | 2026-05-19 | Agent (architect) | 백엔드 스택 전환: Express+Prisma+SQLite -> Spring Boot 3.x+JPA+PostgreSQL, Hexagonal+DDD+Spring Modulith 아키텍처 적용 |
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- BE 5개 서비스 모듈 + FE 4개 페이지 모듈 정의 |

## 1. 모듈 개요

본 문서는 Conduit(RealWorld) 프로젝트의 핵심 모듈을 LLD 수준으로 정의한다. 각 모듈은 07 HLD §1 참조 -- HLD §1 "핵심 모듈/컴포넌트" 표에서 fan-out된 단위이다. 백엔드는 Hexagonal Architecture + DDD + Spring Modulith 패턴을 적용하며, 각 모듈은 domain(model, port), application(service), adapter(in/web, out/persistence) 계층으로 분리된다.

### 1.1 백엔드 모듈

| 모듈 ID | 모듈명 | 책임 | R-ID 매핑 | F-ID 매핑 | HLD 출처 |
|---|---|---|---|---|---|
| BE-USER | User Module | 회원가입, 로그인, JWT 발급/검증, 현재 사용자 조회, 사용자 정보 수정. AggregateRoot: User, VO: Email, Password(BCrypt) | R-F-01, R-F-02, R-F-03, R-F-04 | F-01, F-02 | 07 HLD §1 참조 -- Auth Controller/Service, User Controller/Service |
| BE-ARTICLE | Article Module | 아티클 CRUD, slug 생성, 목록/피드 조회, 페이지네이션, 즐겨찾기 관리. AggregateRoot: Article, VO: Slug, Relationship: Favorite | R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-17, R-F-18, R-N-05, R-N-06 | F-05, F-06, F-07, F-09 | 07 HLD §1 참조 -- Article Controller/Service |
| BE-COMMENT | Comment Module | 댓글 추가, 목록 조회, 댓글 삭제, 작성자 권한 검증. Entity: Comment | R-F-14, R-F-15, R-F-16 | F-08 | 07 HLD §1 참조 -- Comment Controller/Service |
| BE-PROFILE | Profile Module | 프로필 조회, 팔로우/언팔로우, following 상태 판정. Read Model: Profile, Relationship: Follow | R-F-05, R-F-06, R-F-07 | F-03, F-04 | 07 HLD §1 참조 -- Profile Controller/Service |
| BE-TAG | Tag Module | 태그 목록 조회, 아티클-태그 연결 관리. Entity: Tag | R-F-19 | F-10 | 07 HLD §1 참조 -- Tag Controller/Service |

### 1.2 백엔드 공통 모듈

| 모듈 ID | 모듈명 | 책임 | R-ID 매핑 | F-ID 매핑 | HLD 출처 |
|---|---|---|---|---|---|
| BE-SHARED | Shared Infrastructure | Spring Security 설정(SecurityConfig), JWT 인증 필터(JwtAuthenticationFilter extends OncePerRequestFilter), JWT 토큰 제공자(JwtTokenProvider), 전역 예외 처리(@RestControllerAdvice GlobalExceptionHandler), CORS 설정(CorsConfig), OpenAPI 설정(@OpenAPIDefinition OpenApiConfig) | R-N-01, R-N-02, R-N-03, R-N-04 | -- | 07 HLD §1 참조 -- JWT Middleware, Error Handler, CORS Middleware |

### 1.3 프론트엔드 모듈

| 모듈 ID | 모듈명 | 책임 | R-ID 매핑 | F-ID 매핑 | HLD 출처 |
|---|---|---|---|---|---|
| FE-AUTH | Auth Pages | 회원가입(/register), 로그인(/login), 설정(/settings) 화면 및 인증 상태 관리(JWT localStorage) | R-F-01, R-F-02, R-F-03, R-F-04, R-N-03 | F-01, F-02, F-03 | 07 HLD §1 참조 -- FE Auth 컴포넌트 |
| FE-ARTICLE | Article Pages | 아티클 상세(/article/:slug), 에디터(/editor, /editor/:slug) 화면, 마크다운 렌더링 | R-F-10, R-F-11, R-F-12, R-F-13 | F-07 | 07 HLD §1 참조 -- FE Article 컴포넌트 |
| FE-FEED | Feed/Home Page | 홈(/) 화면, Your Feed/Global Feed/Tag 탭, 아티클 카드 목록, 사이드바 태그, 페이지네이션 | R-F-08, R-F-09, R-F-17, R-F-18, R-F-19, R-N-05 | F-05, F-06, F-09, F-10 | 07 HLD §1 참조 -- FE Feed 컴포넌트 |
| FE-PROFILE | Profile Page | 프로필(/profile/:username, /profile/:username/favorites) 화면, 팔로우/언팔로우 UI, 사용자 아티클/즐겨찾기 탭 | R-F-05, R-F-06, R-F-07 | F-03, F-04 | 07 HLD §1 참조 -- FE Profile 컴포넌트 |

### 1.4 프론트엔드 공통 모듈

| 모듈 ID | 모듈명 | 책임 | R-ID 매핑 | F-ID 매핑 | HLD 출처 |
|---|---|---|---|---|---|
| FE-API | API Client | Axios/fetch 래퍼, base URL 설정(http://localhost:8080/api), JWT 헤더 자동 부착, 에러 응답 파싱 | R-N-01, R-N-02, R-N-03 | -- | 07 HLD §1 참조 -- FE API Client |
| FE-ROUTER | Router | React Router 해시 라우팅, 인증 가드, 레이아웃(Header/Footer) 래퍼 | -- | -- | 07 HLD §1 참조 -- FE Router |

---

## 2. 외부 인터페이스

### 2.1 BE-USER 외부 인터페이스

| 인터페이스 | 입력 | 출력 | 에러 |
|---|---|---|---|
| `POST /api/users` (회원가입) | `{ "user": { "username", "email", "password" } }` | `{ "user": { email, token, username, bio, image } }` | 422: 중복 email/username, 필수 필드 누락 |
| `POST /api/users/login` (로그인) | `{ "user": { "email", "password" } }` | `{ "user": { email, token, username, bio, image } }` | 401/422: 잘못된 credentials |
| `GET /api/user` (현재 사용자) | Header: `Authorization: Token <jwt>` | `{ "user": { email, token, username, bio, image } }` | 401: 인증 실패 |
| `PUT /api/user` (사용자 수정) | `{ "user": { email?, username?, password?, bio?, image? } }` + JWT | `{ "user": { email, token, username, bio, image } }` | 401: 미인증; 422: 중복 email/username |

### 2.2 BE-ARTICLE 외부 인터페이스

| 인터페이스 | 입력 | 출력 | 에러 |
|---|---|---|---|
| `GET /api/articles` (목록) | Query: tag?, author?, favorited?, limit(=20), offset(=0); Auth optional | `{ "articles": Article[], "articlesCount": number }` (body 미포함) | -- |
| `GET /api/articles/feed` (피드) | Query: limit(=20), offset(=0); Auth required | `{ "articles": Article[], "articlesCount": number }` (body 미포함) | 401: 미인증 |
| `GET /api/articles/:slug` (단건) | Path: slug; Auth optional | `{ "article": Article }` (body 포함) | 404: 미존재 slug |
| `POST /api/articles` (생성) | `{ "article": { "title", "description", "body", "tagList"? } }` + JWT | `{ "article": Article }` | 401: 미인증; 422: 필수 필드 누락 |
| `PUT /api/articles/:slug` (수정) | `{ "article": { title?, description?, body? } }` + JWT | `{ "article": Article }` | 401: 미인증; 403: 타인 아티클; 404: 미존재 |
| `DELETE /api/articles/:slug` (삭제) | Path: slug; Auth required | 200/204 | 401: 미인증; 403: 타인 아티클; 404: 미존재 |
| `POST /api/articles/:slug/favorite` (즐겨찾기) | Path: slug; Auth required | `{ "article": Article }` (favorited=true) | 401: 미인증; 404: 미존재 |
| `DELETE /api/articles/:slug/favorite` (즐겨찾기 해제) | Path: slug; Auth required | `{ "article": Article }` (favorited=false) | 401: 미인증; 404: 미존재 |

### 2.3 BE-COMMENT 외부 인터페이스

| 인터페이스 | 입력 | 출력 | 에러 |
|---|---|---|---|
| `POST /api/articles/:slug/comments` (추가) | `{ "comment": { "body" } }` + JWT | `{ "comment": Comment }` | 401: 미인증; 404: 미존재 slug; 422: body 빈 값 |
| `GET /api/articles/:slug/comments` (목록) | Path: slug; Auth optional | `{ "comments": Comment[] }` | 404: 미존재 slug |
| `DELETE /api/articles/:slug/comments/:id` (삭제) | Path: slug, id; Auth required | 200/204 | 401: 미인증; 403: 타인 댓글; 404: 미존재 |

### 2.4 BE-PROFILE 외부 인터페이스

| 인터페이스 | 입력 | 출력 | 에러 |
|---|---|---|---|
| `GET /api/profiles/:username` (조회) | Path: username; Auth optional | `{ "profile": { username, bio, image, following } }` | 404: 미존재 username |
| `POST /api/profiles/:username/follow` (팔로우) | Path: username; Auth required | `{ "profile": Profile }` (following=true) | 401: 미인증; 404: 미존재 |
| `DELETE /api/profiles/:username/follow` (언팔로우) | Path: username; Auth required | `{ "profile": Profile }` (following=false) | 401: 미인증; 404: 미존재 |

### 2.5 BE-TAG 외부 인터페이스

| 인터페이스 | 입력 | 출력 | 에러 |
|---|---|---|---|
| `GET /api/tags` (태그 목록) | 없음; Auth not required | `{ "tags": string[] }` | 500: 서버 내부 에러 |

### 2.6 FE-API 외부 인터페이스 (API Client)

| 인터페이스 | 입력 | 출력 | 에러 |
|---|---|---|---|
| `api.get(url, params?)` | URL 경로, 쿼리 파라미터 | JSON 응답 객체 | AxiosError (status, errors 객체) |
| `api.post(url, data?)` | URL 경로, 요청 body | JSON 응답 객체 | AxiosError (status, errors 객체) |
| `api.put(url, data?)` | URL 경로, 요청 body | JSON 응답 객체 | AxiosError (status, errors 객체) |
| `api.delete(url)` | URL 경로 | void / 204 | AxiosError (status, errors 객체) |

---

## 3. 내부 컴포넌트

### 3.1 BE-USER 내부 컴포넌트 (Hexagonal + DDD)

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `User` | Domain Model (AggregateRoot) | 사용자 도메인 엔티티 -- id, email, username, password, bio, image 관리 | Email (VO), Password (VO) |
| `Email` | Value Object | 이메일 형식 검증, 불변 값 타입 | -- |
| `Password` | Value Object | BCrypt 해시된 비밀번호 래핑, 평문 비교 메서드 | -- |
| `RegisterUserUseCase` | Port (Inbound) | 회원가입 유스케이스 인터페이스 | -- |
| `LoginUserUseCase` | Port (Inbound) | 로그인 유스케이스 인터페이스 | -- |
| `GetCurrentUserUseCase` | Port (Inbound) | 현재 사용자 조회 유스케이스 인터페이스 | -- |
| `UpdateUserUseCase` | Port (Inbound) | 사용자 정보 수정 유스케이스 인터페이스 | -- |
| `UserRepository` | Port (Outbound) | 사용자 영속화 포트 인터페이스 -- findByEmail, findByUsername, save | -- |
| `PasswordEncoder` | Port (Outbound) | 비밀번호 암호화 포트 인터페이스 -- encode, matches | -- |
| `UserService` | Application Service | 모든 인바운드 포트 구현체 -- 가입 검증, 비밀번호 해싱, JWT 생성 | UserRepository, PasswordEncoder, JwtTokenProvider |
| `UserController` | Adapter (Inbound/Web) | @RestController -- /api/users, /api/user 엔드포인트 핸들링 | RegisterUserUseCase, LoginUserUseCase, GetCurrentUserUseCase, UpdateUserUseCase |
| `UserJpaEntity` | Adapter (Outbound/Persistence) | JPA @Entity 매핑 -- users 테이블 | -- |
| `UserJpaRepository` | Adapter (Outbound/Persistence) | Spring Data JpaRepository 인터페이스 | -- |
| `UserPersistenceAdapter` | Adapter (Outbound/Persistence) | UserRepository 포트 구현체 -- JPA 엔티티 <-> 도메인 모델 변환 | UserJpaRepository |

### 3.2 BE-ARTICLE 내부 컴포넌트 (Hexagonal + DDD)

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `Article` | Domain Model (AggregateRoot) | 아티클 도메인 엔티티 -- title, description, body, slug, author, tags, createdAt, updatedAt | Slug (VO) |
| `Slug` | Value Object | URL-safe slug 값 타입 -- title에서 생성, 불변 | -- |
| `Favorite` | Domain Relationship | 사용자-아티클 즐겨찾기 관계 도메인 표현 | -- |
| `CreateArticleUseCase` | Port (Inbound) | 아티클 생성 유스케이스 인터페이스 | -- |
| `UpdateArticleUseCase` | Port (Inbound) | 아티클 수정 유스케이스 인터페이스 | -- |
| `DeleteArticleUseCase` | Port (Inbound) | 아티클 삭제 유스케이스 인터페이스 | -- |
| `GetArticleUseCase` | Port (Inbound) | 아티클 단건 조회 유스케이스 인터페이스 | -- |
| `ListArticlesUseCase` | Port (Inbound) | 아티클 목록 조회 유스케이스 인터페이스 (필터, 페이지네이션) | -- |
| `FeedArticlesUseCase` | Port (Inbound) | 팔로잉 피드 조회 유스케이스 인터페이스 | -- |
| `FavoriteArticleUseCase` | Port (Inbound) | 즐겨찾기 추가 유스케이스 인터페이스 | -- |
| `UnfavoriteArticleUseCase` | Port (Inbound) | 즐겨찾기 해제 유스케이스 인터페이스 | -- |
| `ArticleRepository` | Port (Outbound) | 아티클 영속화 포트 인터페이스 -- findBySlug, save, delete, findAll(filter) | -- |
| `FavoriteRepository` | Port (Outbound) | 즐겨찾기 영속화 포트 인터페이스 -- save, delete, existsByUserAndArticle, countByArticle | -- |
| `ArticleService` | Application Service | 모든 인바운드 포트 구현체 -- slug 생성, 권한 검증, 필터/페이지네이션 | ArticleRepository, FavoriteRepository |
| `ArticleController` | Adapter (Inbound/Web) | @RestController -- /api/articles, /api/articles/:slug, /api/articles/feed, /api/articles/:slug/favorite | 모든 Article UseCase 인터페이스 |
| `ArticleJpaEntity` | Adapter (Outbound/Persistence) | JPA @Entity 매핑 -- articles 테이블, @Version 낙관적 잠금 | -- |
| `ArticleJpaRepository` | Adapter (Outbound/Persistence) | Spring Data JpaRepository 인터페이스 -- JPQL 커스텀 쿼리 포함 | -- |
| `ArticlePersistenceAdapter` | Adapter (Outbound/Persistence) | ArticleRepository, FavoriteRepository 포트 구현체 | ArticleJpaRepository |

### 3.3 BE-COMMENT 내부 컴포넌트 (Hexagonal + DDD)

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `Comment` | Domain Model (Entity) | 댓글 도메인 엔티티 -- body, author, article, createdAt, updatedAt | -- |
| `AddCommentUseCase` | Port (Inbound) | 댓글 추가 유스케이스 인터페이스 | -- |
| `ListCommentsUseCase` | Port (Inbound) | 댓글 목록 조회 유스케이스 인터페이스 | -- |
| `DeleteCommentUseCase` | Port (Inbound) | 댓글 삭제 유스케이스 인터페이스 | -- |
| `CommentRepository` | Port (Outbound) | 댓글 영속화 포트 인터페이스 -- save, findByArticleSlug, deleteById | -- |
| `CommentService` | Application Service | 모든 인바운드 포트 구현체 -- 댓글 생성, 작성자 권한 검증, 아티클 존재 확인 | CommentRepository, ArticleRepository |
| `CommentController` | Adapter (Inbound/Web) | @RestController -- /api/articles/:slug/comments | 모든 Comment UseCase 인터페이스 |
| `CommentJpaEntity` | Adapter (Outbound/Persistence) | JPA @Entity 매핑 -- comments 테이블 | -- |
| `CommentJpaRepository` | Adapter (Outbound/Persistence) | Spring Data JpaRepository 인터페이스 | -- |
| `CommentPersistenceAdapter` | Adapter (Outbound/Persistence) | CommentRepository 포트 구현체 | CommentJpaRepository |

### 3.4 BE-PROFILE 내부 컴포넌트 (Hexagonal + DDD)

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `Profile` | Domain Read Model | 프로필 읽기 모델 -- username, bio, image, following 상태 | -- |
| `Follow` | Domain Relationship | 팔로우 관계 도메인 표현 -- followerId, followeeId | -- |
| `GetProfileUseCase` | Port (Inbound) | 프로필 조회 유스케이스 인터페이스 | -- |
| `FollowUserUseCase` | Port (Inbound) | 팔로우 유스케이스 인터페이스 | -- |
| `UnfollowUserUseCase` | Port (Inbound) | 언팔로우 유스케이스 인터페이스 | -- |
| `FollowRepository` | Port (Outbound) | 팔로우 관계 영속화 포트 인터페이스 -- save, delete, existsByFollowerAndFollowee | -- |
| `ProfileService` | Application Service | 모든 인바운드 포트 구현체 -- 프로필 조회, Follow 관계 생성/삭제 | FollowRepository, UserRepository |
| `ProfileController` | Adapter (Inbound/Web) | @RestController -- /api/profiles/:username, /api/profiles/:username/follow | 모든 Profile UseCase 인터페이스 |
| `FollowJpaEntity` | Adapter (Outbound/Persistence) | JPA @Entity 매핑 -- follows 테이블 | -- |
| `FollowJpaRepository` | Adapter (Outbound/Persistence) | Spring Data JpaRepository 인터페이스 | -- |
| `ProfilePersistenceAdapter` | Adapter (Outbound/Persistence) | FollowRepository 포트 구현체 | FollowJpaRepository |

### 3.5 BE-TAG 내부 컴포넌트 (Hexagonal + DDD)

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `Tag` | Domain Model (Entity) | 태그 도메인 엔티티 -- name | -- |
| `ListTagsUseCase` | Port (Inbound) | 태그 목록 조회 유스케이스 인터페이스 | -- |
| `TagRepository` | Port (Outbound) | 태그 영속화 포트 인터페이스 -- findAll, findByName, save | -- |
| `TagService` | Application Service | 인바운드 포트 구현체 -- 사용 중인 태그 목록 집계 | TagRepository |
| `TagController` | Adapter (Inbound/Web) | @RestController -- /api/tags | ListTagsUseCase |
| `TagJpaEntity` | Adapter (Outbound/Persistence) | JPA @Entity 매핑 -- tags 테이블 | -- |
| `TagJpaRepository` | Adapter (Outbound/Persistence) | Spring Data JpaRepository 인터페이스 | -- |
| `TagPersistenceAdapter` | Adapter (Outbound/Persistence) | TagRepository 포트 구현체 | TagJpaRepository |

### 3.6 BE-SHARED 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `SecurityConfig` | Infrastructure (Security) | Spring Security 6.x 설정 -- SecurityFilterChain 빈 정의, 엔드포인트별 인증 규칙, CSRF 비활성, stateless 세션 | JwtAuthenticationFilter |
| `JwtAuthenticationFilter` | Infrastructure (Security) | OncePerRequestFilter 확장 -- Authorization 헤더에서 Token 파싱, JWT 검증, SecurityContext에 Authentication 주입 | JwtTokenProvider |
| `JwtTokenProvider` | Infrastructure (Security) | JWT 생성/검증/파싱 -- jjwt 라이브러리 사용, HS256 서명, userId 클레임 추출 | jjwt |
| `GlobalExceptionHandler` | Infrastructure (Exception) | @RestControllerAdvice -- 전역 예외 핸들러, ApiException 계층 매핑, RealWorld 에러 형식 직렬화 | -- |
| `ApiException` | Infrastructure (Exception) | 커스텀 예외 기반 클래스 -- ValidationException, UnauthorizedException, ForbiddenException, NotFoundException 계층 | -- |
| `ErrorResponse` | Infrastructure (Exception) | 에러 응답 DTO -- `{ "errors": { "field": ["message"] } }` 형식 | -- |
| `CorsConfig` | Infrastructure (Config) | CORS 헤더 설정 -- localhost FE(5173) 허용, WebMvcConfigurer 구현 | -- |
| `OpenApiConfig` | Infrastructure (Config) | @OpenAPIDefinition -- Springdoc OpenAPI 설정, API 문서 자동 생성 | springdoc-openapi |

### 3.7 FE-AUTH 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `LoginPage` | React Page | 로그인 폼 렌더링, 로그인 API 호출, JWT 저장 후 홈 리다이렉트 | api client, auth store |
| `RegisterPage` | React Page | 회원가입 폼 렌더링, 가입 API 호출, JWT 저장 후 홈 리다이렉트 | api client, auth store |
| `SettingsPage` | React Page | 프로필 수정 폼, 사용자 정보 수정 API 호출, 로그아웃 | api client, auth store |
| `useAuth` | Hook/Store | 인증 상태(currentUser, token) 관리, localStorage 동기화 | -- |

### 3.8 FE-ARTICLE 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `ArticlePage` | React Page | 아티클 상세 -- 배너, 본문(마크다운), 작성자 정보, 편집/삭제 버튼, 댓글 섹션 | api client, markdown renderer |
| `EditorPage` | React Page | 아티클 작성/편집 폼, 태그 입력, publish/update 호출 | api client, auth store |
| `ArticleMeta` | React Component | 작성자 아바타/이름/날짜 + 팔로우/즐겨찾기/편집/삭제 버튼 바 | api client |
| `CommentSection` | React Component | 댓글 폼 + 댓글 목록 렌더링, 삭제 기능 | api client, auth store |
| `CommentCard` | React Component | 단일 댓글 카드 -- 작성자 정보, body, 삭제 아이콘 | -- |

### 3.9 FE-FEED 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `HomePage` | React Page | 홈 레이아웃 -- 배너, 피드 탭, 아티클 리스트, 사이드바, 페이지네이션 | api client, auth store |
| `FeedToggle` | React Component | Your Feed / Global Feed / Tag 탭 토글 | -- |
| `ArticleList` | React Component | 아티클 카드 반복 렌더링 + 로딩/빈 상태 처리 | -- |
| `ArticlePreview` | React Component | 단일 아티클 카드 -- 작성자, 날짜, 제목, 설명, 태그, 즐겨찾기 버튼 | api client |
| `TagSidebar` | React Component | 인기 태그 목록 + 클릭 필터 | api client |
| `Pagination` | React Component | offset 기반 페이지 번호 렌더링 | -- |

### 3.10 FE-PROFILE 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `ProfilePage` | React Page | 프로필 배너 + My Articles / Favorited Articles 탭 + 아티클 리스트 | api client, auth store |
| `ProfileBanner` | React Component | 사용자 이미지/이름/bio + 팔로우/설정 버튼 | api client |
| `ArticleToggle` | React Component | My Articles / Favorited Articles 탭 토글 | -- |

---

## 4. 데이터 흐름

### 4.1 Hexagonal 계층 흐름 (공통 패턴)

모든 백엔드 요청은 다음 Hexagonal 흐름을 따른다.

```
HTTP Request
   │
   ▼
[SecurityFilterChain]
   ├─ CorsFilter (CORS 헤더 처리)
   └─ JwtAuthenticationFilter (Token 파싱 → SecurityContext 주입)
   │
   ▼
[Controller (Adapter In/Web)]
   │  @RestController -- 요청 DTO 역직렬화, 입력 검증(@Valid)
   │
   ▼
[UseCase (Port Inbound)]
   │  인터페이스 호출 -- Controller는 Port만 의존
   │
   ▼
[Service (Application)]
   │  비즈니스 로직 -- @Transactional, 도메인 모델 조작
   │
   ▼
[Repository (Port Outbound)]
   │  인터페이스 호출 -- Service는 Port만 의존
   │
   ▼
[PersistenceAdapter (Adapter Out/Persistence)]
   │  JPA Entity <-> Domain Model 변환, Spring Data JPA 위임
   │
   ▼
[PostgreSQL]
```

### 4.2 회원가입 흐름 (R-F-01, F-01)

```
User -> RegisterPage: 폼 입력 (username, email, password)
RegisterPage -> api.post("/api/users"): HTTP POST
SecurityFilterChain -> JwtAuthenticationFilter: 토큰 없음 (공개 엔드포인트)
Request -> UserController.register: @PostMapping("/api/users")
UserController -> RegisterUserUseCase.register: 포트 호출
RegisterUserUseCase -> UserService.register: 구현체
UserService -> PasswordEncoder.encode: BCrypt 해싱 (Port)
UserService -> UserRepository.save: 사용자 저장 (Port)
UserRepository -> UserPersistenceAdapter: JPA Entity 변환 + 저장
UserPersistenceAdapter -> UserJpaRepository.save: Spring Data JPA
UserJpaRepository -> PostgreSQL: INSERT
UserService -> JwtTokenProvider.generateToken: JWT 생성
UserService -->> UserController: User + token
UserController -->> RegisterPage: 200 { user }
RegisterPage -> useAuth.setUser: JWT localStorage 저장
RegisterPage -> Router.navigate("/"): 홈 리다이렉트
```

### 4.3 로그인 흐름 (R-F-02, F-02)

```
User -> LoginPage: 폼 입력 (email, password)
LoginPage -> api.post("/api/users/login"): HTTP POST
Request -> UserController.login: @PostMapping("/api/users/login")
UserController -> LoginUserUseCase.login: 포트 호출
LoginUserUseCase -> UserService.login: 구현체
UserService -> UserRepository.findByEmail: 이메일로 조회 (Port)
UserRepository -> UserPersistenceAdapter -> PostgreSQL: SELECT
UserService -> PasswordEncoder.matches: BCrypt 비교 (Port)
UserService -> JwtTokenProvider.generateToken: JWT 생성
UserService -->> UserController: User + token 또는 예외
UserController -->> LoginPage: 200 { user } 또는 401/422
LoginPage -> useAuth.setUser: JWT localStorage 저장
LoginPage -> Router.navigate("/"): 홈 리다이렉트
```

### 4.4 아티클 목록/피드 조회 흐름 (R-F-08, R-F-09, F-05, F-06)

```
User -> HomePage: 탭 선택 (Global Feed / Your Feed / Tag)
HomePage -> api.get("/api/articles" 또는 "/api/articles/feed"): 쿼리 파라미터 포함
SecurityFilterChain -> JwtAuthenticationFilter: 토큰 파싱 (optional/required)
Request -> ArticleController.list 또는 .feed: @GetMapping
ArticleController -> ListArticlesUseCase 또는 FeedArticlesUseCase: 포트 호출
UseCase -> ArticleService: 구현체 -- 필터 + 페이지네이션
ArticleService -> ArticleRepository.findAll(filter, pageable): 조건부 조회 (Port)
ArticleRepository -> ArticlePersistenceAdapter -> PostgreSQL: SELECT + COUNT
ArticleService -->> ArticleController: { articles[], articlesCount }
ArticleController -->> HomePage: 200 응답
HomePage -> ArticleList: 카드 렌더링
HomePage -> Pagination: 페이지 번호 렌더링
```

### 4.5 아티클 생성 흐름 (R-F-11, F-07)

```
User -> EditorPage: 폼 입력 (title, description, body, tagList)
EditorPage -> api.post("/api/articles"): HTTP POST
SecurityFilterChain -> JwtAuthenticationFilter: JWT 검증 -> SecurityContext
Request -> ArticleController.create: @PostMapping("/api/articles")
ArticleController -> CreateArticleUseCase.create: 포트 호출
CreateArticleUseCase -> ArticleService.create: 구현체 (@Transactional)
ArticleService -> Slug.fromTitle(title): VO 생성 (title -> slug 변환)
ArticleService -> ArticleRepository.save: 아티클 + 태그 연결 저장 (Port)
ArticleRepository -> ArticlePersistenceAdapter -> PostgreSQL: INSERT (트랜잭션)
ArticleService -->> ArticleController: Article 객체
ArticleController -->> EditorPage: 200 { article }
EditorPage -> Router.navigate("/article/:slug"): 상세 페이지 이동
```

### 4.6 댓글 추가 흐름 (R-F-14, F-08)

```
User -> CommentSection: 댓글 body 입력, "Post Comment" 클릭
CommentSection -> api.post("/api/articles/:slug/comments"): HTTP POST
SecurityFilterChain -> JwtAuthenticationFilter: JWT 검증
Request -> CommentController.add: @PostMapping
CommentController -> AddCommentUseCase.addComment: 포트 호출
AddCommentUseCase -> CommentService.addComment: 구현체 (@Transactional)
CommentService -> ArticleRepository.findBySlug: slug로 아티클 존재 확인
CommentService -> CommentRepository.save: 댓글 저장 (Port)
CommentRepository -> CommentPersistenceAdapter -> PostgreSQL: INSERT
CommentService -->> CommentController: Comment 객체
CommentController -->> CommentSection: 200 { comment }
CommentSection -> 댓글 목록 갱신: 새 댓글 prepend
```

### 4.7 팔로우/언팔로우 흐름 (R-F-06, R-F-07, F-04)

```
User -> ProfileBanner: "Follow" 또는 "Unfollow" 버튼 클릭
ProfileBanner -> api.post/delete("/api/profiles/:username/follow"): HTTP POST/DELETE
SecurityFilterChain -> JwtAuthenticationFilter: JWT 검증
Request -> ProfileController.follow/unfollow: @PostMapping/@DeleteMapping
ProfileController -> FollowUserUseCase/UnfollowUserUseCase: 포트 호출
UseCase -> ProfileService: 구현체 (@Transactional)
ProfileService -> UserRepository.findByUsername: username 존재 확인
ProfileService -> FollowRepository.save/delete: Follow 관계 생성/삭제 (Port)
FollowRepository -> ProfilePersistenceAdapter -> PostgreSQL: INSERT/DELETE
ProfileService -->> ProfileController: Profile (following=true/false)
ProfileController -->> ProfileBanner: 200 { profile }
ProfileBanner -> 버튼 토글: Follow <-> Unfollow
```

### 4.8 즐겨찾기 토글 흐름 (R-F-17, R-F-18, F-09)

```
User -> ArticlePreview 또는 ArticleMeta: 하트 버튼 클릭
Component -> api.post/delete("/api/articles/:slug/favorite"): HTTP POST/DELETE
SecurityFilterChain -> JwtAuthenticationFilter: JWT 검증
Request -> ArticleController.favorite/unfavorite: @PostMapping/@DeleteMapping
ArticleController -> FavoriteArticleUseCase/UnfavoriteArticleUseCase: 포트 호출
UseCase -> ArticleService: 구현체 (@Transactional)
ArticleService -> FavoriteRepository.save/delete: Favorite 관계 토글 (Port)
ArticleService -> FavoriteRepository.countByArticle: favoritesCount 재계산
FavoriteRepository -> ArticlePersistenceAdapter -> PostgreSQL: INSERT/DELETE + COUNT
ArticleService -->> ArticleController: Article (favorited, favoritesCount)
ArticleController -->> Component: 200 { article }
Component -> UI 갱신: 하트 활성/비활성 + 카운트 갱신
```

---

## 5. 상태·라이프사이클

### 5.1 인증 상태 (FE-AUTH)

```
[비인증] --(회원가입/로그인 성공)--> [인증됨]
[인증됨] --(로그아웃)--> [비인증]
[인증됨] --(JWT 만료/변조)--> [비인증] (API 401 응답 시 자동 전이)
[인증됨] --(설정 수정)--> [인증됨] (토큰 갱신)
```

- 비인증 상태: 네비게이션에 Home / Sign in / Sign up 표시. Your Feed 비활성.
- 인증 상태: 네비게이션에 Home / New Article / Settings / Profile 표시. Your Feed 활성.

### 5.2 아티클 라이프사이클 (BE-ARTICLE)

```
[미존재] --(POST /api/articles)--> [생성됨]
[생성됨] --(PUT /api/articles/:slug)--> [수정됨] (slug 재생성 가능)
[생성됨/수정됨] --(DELETE /api/articles/:slug)--> [삭제됨] (연관 댓글 cascade)
```

### 5.3 Follow 관계 라이프사이클 (BE-PROFILE)

```
[미팔로우] --(POST .../follow)--> [팔로잉]
[팔로잉] --(DELETE .../follow)--> [미팔로우]
```

### 5.4 Favorite 관계 라이프사이클 (BE-ARTICLE)

```
[미즐겨찾기] --(POST .../favorite)--> [즐겨찾기됨] (favoritesCount + 1)
[즐겨찾기됨] --(DELETE .../favorite)--> [미즐겨찾기] (favoritesCount - 1)
```

### 5.5 Spring Application 라이프사이클 (BE)

```
[미기동] --(./gradlew bootRun)--> [Flyway 마이그레이션 실행] --> [Spring Context 초기화] --> [기동 완료]
[기동 완료] --(SIGTERM/SIGINT)--> [Graceful Shutdown] --> [미기동]
```

---

## 6. 에러 처리

### 6.1 백엔드 에러 분류 (@RestControllerAdvice)

| 에러 | 발생 조건 | 처리 |
|---|---|---|
| 401 Unauthorized (UnauthorizedException) | JWT 없음, 만료, 변조. JwtAuthenticationFilter에서 인증 실패 | `{ "errors": { "token": ["is invalid"] } }` 반환. SecurityFilterChain에서 즉시 차단, AuthenticationEntryPoint 위임 |
| 403 Forbidden (ForbiddenException) | 타인 아티클 수정/삭제, 타인 댓글 삭제 시도 | `{ "errors": { "article": ["not owned by you"] } }` 반환. Service 레이어에서 소유자 검증 후 throw |
| 404 Not Found (NotFoundException) | 미존재 slug, username, comment id | `{ "errors": { "resource": ["not found"] } }` 반환. Service 레이어에서 조회 실패 시 throw |
| 422 Unprocessable Entity (ValidationException) | 필수 필드 누락, 중복 email/username, body 빈 값 | `{ "errors": { "field": ["error message"] } }` 반환. @Valid Bean Validation 또는 JPA unique constraint violation(DataIntegrityViolationException) |
| 500 Internal Server Error | 예기치 않은 서버 에러, DB 연결 실패 | `{ "errors": { "server": ["internal error"] } }` 반환. GlobalExceptionHandler에서 Exception catch-all |

### 6.2 백엔드 예외 계층 구조

```
ApiException (abstract, RuntimeException)
  ├── ValidationException (422)
  ├── UnauthorizedException (401)
  ├── ForbiddenException (403)
  └── NotFoundException (404)
```

GlobalExceptionHandler(@RestControllerAdvice)가 각 예외 타입별 @ExceptionHandler 메서드를 정의하고, Spring의 MethodArgumentNotValidException(Bean Validation 실패), DataIntegrityViolationException(JPA unique constraint) 등도 422 형식으로 매핑한다.

### 6.3 프론트엔드 에러 분류

| 에러 | 발생 조건 | 처리 |
|---|---|---|
| 네트워크 에러 | BE 서버 미기동, 네트워크 단절 | 사용자에게 "서버에 연결할 수 없습니다" 토스트/메시지 표시 |
| 401 응답 | JWT 만료 또는 변조 | useAuth에서 토큰 삭제 + 로그인 페이지 리다이렉트 |
| 422 응답 | 입력 검증 실패 | 에러 객체에서 필드별 메시지 추출 -> 폼 상단에 에러 리스트 렌더링 |
| 403 응답 | 권한 없는 작업 시도 | "권한이 없습니다" 메시지 표시 (정상 흐름에서는 버튼 미표시로 예방) |
| 404 응답 | 미존재 리소스 접근 | "찾을 수 없습니다" 메시지 또는 홈으로 리다이렉트 |

### 6.4 공통 에러 응답 형식 (R-N-02)

모든 백엔드 에러 응답은 다음 RealWorld 표준 형식을 따른다.

```json
{
  "errors": {
    "field_name": ["error message 1", "error message 2"]
  }
}
```

GlobalExceptionHandler(@RestControllerAdvice)가 throw된 ApiException 및 Spring 내장 예외를 catch하여 위 형식의 ErrorResponse DTO로 직렬화한다. JPA의 DataIntegrityViolationException(unique constraint P23505)도 422 형식으로 변환한다.

---

## 7. 동시성·트랜잭션

### 7.1 트랜잭션 관리 (Spring @Transactional)

- **모든 Service 메서드**: @Transactional 어노테이션 적용. 읽기 전용 메서드는 `@Transactional(readOnly = true)`로 최적화.
- **아티클 생성 (BE-ARTICLE)**: Article 레코드 + Tag 레코드 + article_tags 관계를 단일 트랜잭션으로 처리. Tag가 미존재 시 신규 생성 포함.
- **아티클 삭제 (BE-ARTICLE)**: 연관 댓글, 즐겨찾기, 아티클-태그 관계를 JPA cascade(CascadeType.REMOVE) + 외래 키 ON DELETE CASCADE로 DB 수준에서 보장.
- **즐겨찾기 토글 (BE-ARTICLE)**: Favorite 레코드 생성/삭제와 favoritesCount 갱신의 정합성. JPQL COUNT 쿼리로 실시간 count 계산.

### 7.2 동시성 제어

- **Optimistic Locking**: Article, User 엔티티에 `@Version` 필드 적용. 동시 수정 시 OptimisticLockingFailureException 발생 -> GlobalExceptionHandler에서 409 Conflict 반환.
- **PostgreSQL Row-Level Locking**: 필요 시 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 사용 가능. 현 단계에서는 낙관적 잠금이 기본.
- **Slug 중복**: 같은 title로 동시 생성 시 DB unique constraint 위반. Slug VO가 suffix(-1, -2 등)를 추가하되, 최종적으로 PostgreSQL unique constraint가 안전망 역할.
- **JWT 무상태**: 서버 세션 없음. JWT 검증은 stateless이므로 동시성 이슈 없음.
- **커넥션 풀**: HikariCP(Spring Boot 기본) 사용. 기본 pool size 10, 로컬 환경에서 충분.

### 7.3 트랜잭션 격리 수준

PostgreSQL 기본 격리 수준 READ COMMITTED를 사용한다. 팬텀 리드가 문제되는 시나리오(예: articlesCount 정확성)는 현 단계에서 허용 범위로 판단하며, 추후 REPEATABLE READ 전환을 검토할 수 있다.

---

## 8. 테스트 진입점

### 8.1 백엔드 단위 테스트 (JUnit 5 + Mockito)

| 모듈 | 테스트 대상 | 테스트 파일 (예상) | 검증 항목 |
|---|---|---|---|
| BE-USER | UserService.register | `src/test/java/.../user/application/service/UserServiceTest.java` | BCrypt 해싱 호출, JWT 생성, 중복 email/username 검증, UserRepository.save 호출 |
| BE-USER | UserService.login | `src/test/java/.../user/application/service/UserServiceTest.java` | PasswordEncoder.matches 호출, JWT 생성, 미존재 사용자 UnauthorizedException |
| BE-USER | JwtTokenProvider | `src/test/java/.../shared/security/JwtTokenProviderTest.java` | 토큰 생성/파싱 성공, 만료 토큰 검증 실패, 변조 토큰 검증 실패 |
| BE-ARTICLE | ArticleService.create | `src/test/java/.../article/application/service/ArticleServiceTest.java` | Slug 생성, 태그 연결, 필수 필드 검증, ArticleRepository.save 호출 |
| BE-ARTICLE | ArticleService.list/feed | `src/test/java/.../article/application/service/ArticleServiceTest.java` | 필터 조합, 페이지네이션, body 미포함 |
| BE-ARTICLE | Slug (VO) | `src/test/java/.../article/domain/model/SlugTest.java` | 한글/특수문자 처리, 중복 suffix, fromTitle 팩토리 메서드 |
| BE-ARTICLE | ArticleService.favorite/unfavorite | `src/test/java/.../article/application/service/ArticleServiceTest.java` | 토글 정합성, favoritesCount 갱신, FavoriteRepository 호출 |
| BE-COMMENT | CommentService.addComment/delete | `src/test/java/.../comment/application/service/CommentServiceTest.java` | 생성 정상, 작성자 권한 검증(ForbiddenException), 아티클 존재 확인(NotFoundException) |
| BE-PROFILE | ProfileService.follow/unfollow | `src/test/java/.../profile/application/service/ProfileServiceTest.java` | Follow 관계 생성/삭제, following 상태 판정, FollowRepository 호출 |
| BE-TAG | TagService.list | `src/test/java/.../tag/application/service/TagServiceTest.java` | 사용 중인 태그만 반환, 빈 결과 처리 |
| BE-SHARED | GlobalExceptionHandler | `src/test/java/.../shared/exception/GlobalExceptionHandlerTest.java` | 422/401/403/404/500 형식 직렬화, DataIntegrityViolationException 변환 |

### 8.2 백엔드 통합 테스트 (@SpringBootTest + MockMvc + Testcontainers)

| 모듈 | 테스트 대상 | 테스트 파일 (예상) | 검증 항목 |
|---|---|---|---|
| BE-USER | POST /api/users, POST /api/users/login, GET /api/user, PUT /api/user | `src/test/java/.../user/adapter/in/web/UserControllerIntegrationTest.java` | HTTP 상태, 응답 형식, 에러 형식, JWT 발급/검증, Testcontainers PostgreSQL |
| BE-ARTICLE | GET/POST/PUT/DELETE /api/articles, favorite/unfavorite | `src/test/java/.../article/adapter/in/web/ArticleControllerIntegrationTest.java` | CRUD 전체 흐름, 필터, 페이지네이션, 권한, Testcontainers PostgreSQL |
| BE-COMMENT | POST/GET/DELETE /api/articles/:slug/comments | `src/test/java/.../comment/adapter/in/web/CommentControllerIntegrationTest.java` | 댓글 전체 흐름, 권한 검증, Testcontainers PostgreSQL |
| BE-PROFILE | GET /api/profiles, follow/unfollow | `src/test/java/.../profile/adapter/in/web/ProfileControllerIntegrationTest.java` | 프로필 조회, 팔로우 토글, following 상태, Testcontainers PostgreSQL |
| BE-TAG | GET /api/tags | `src/test/java/.../tag/adapter/in/web/TagControllerIntegrationTest.java` | 태그 목록 반환 형식, Testcontainers PostgreSQL |

### 8.3 프론트엔드 단위 테스트 (Vitest)

| 모듈 | 테스트 대상 | 테스트 파일 (예상) | 검증 항목 |
|---|---|---|---|
| FE-AUTH | useAuth hook | `src/__tests__/useAuth.test.ts` | 상태 전이(비인증->인증->비인증), localStorage 동기화 |
| FE-AUTH | LoginPage/RegisterPage | `src/__tests__/AuthPages.test.tsx` | 폼 렌더링, 에러 메시지 표시, 리다이렉트 |
| FE-FEED | ArticlePreview | `src/__tests__/ArticlePreview.test.tsx` | 카드 렌더링, 즐겨찾기 버튼 상태 |
| FE-FEED | Pagination | `src/__tests__/Pagination.test.tsx` | 페이지 번호 계산, 클릭 콜백 |
| FE-ARTICLE | CommentSection | `src/__tests__/CommentSection.test.tsx` | 댓글 폼, 댓글 목록 렌더링, 삭제 버튼 조건부 표시 |
| FE-PROFILE | ProfileBanner | `src/__tests__/ProfileBanner.test.tsx` | 팔로우 버튼 토글, 설정 버튼 조건부 표시 |
| FE-API | api client | `src/__tests__/api.test.ts` | JWT 헤더 자동 부착, 에러 응답 파싱, base URL 설정(http://localhost:8080/api) |

### 8.4 E2E 테스트 (Playwright)

| 시나리오 | 커버 모듈 | 검증 항목 |
|---|---|---|
| 회원가입 -> 로그인 -> 프로필 수정 | FE-AUTH + BE-USER | 인증 전체 흐름, 네비게이션 전환 |
| 아티클 생성 -> 조회 -> 수정 -> 삭제 | FE-ARTICLE + BE-ARTICLE | 아티클 CRUD 전체 흐름, slug 생성/갱신 |
| 글로벌 피드 -> 태그 필터 -> 페이지네이션 | FE-FEED + BE-ARTICLE + BE-TAG | 목록 조회, 필터, 페이지 전환 |
| 팔로우 -> Your Feed 확인 | FE-PROFILE + FE-FEED + BE-PROFILE + BE-ARTICLE | 팔로우 후 피드 반영 |
| 댓글 작성 -> 삭제 | FE-ARTICLE + BE-COMMENT | 댓글 전체 흐름 |
| 즐겨찾기 -> 프로필 Favorites 탭 | FE-FEED + FE-PROFILE + BE-ARTICLE | 즐겨찾기 토글, 카운트, 프로필 탭 연동 |
