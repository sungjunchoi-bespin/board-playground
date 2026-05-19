---
doc_type: wbs
version: v0.1
status: Draft
author: sungjun.choi@board-playground.dev
date: 2026-05-19
gate: operations
related:
  R-ID: [R-F-01, R-F-02, R-F-03, R-F-04, R-F-05, R-F-06, R-F-07, R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-14, R-F-15, R-F-16, R-F-17, R-F-18, R-F-19, R-N-01, R-N-02, R-N-03, R-N-04, R-N-05, R-N-06]
  F-ID: [F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10]
  supersedes: null
---

# Conduit (RealWorld) — WBS

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v0.1 | 2026-05-19 | sungjun.choi@board-playground.dev | 초안 -- 5 Sprint, 25 Issue, 전체 R-ID/F-ID 추적성 매트릭스 |

## 0. 개요

본 WBS는 Conduit (RealWorld) 풀스택 프로젝트의 전체 작업 분해 구조를 정의한다. 백엔드(Java 24 + Spring Boot 3 + Hexagonal Architecture + DDD + Spring Modulith + PostgreSQL + Flyway)와 프론트엔드(React 18 + TypeScript + Vite + CSS Modules + Bootstrap 4 CDN)를 Multi-stack Monorepo(`frontend/` + `backend/`)로 구성하며, 로컬 실행 전용(localhost)으로 개발한다. 총 5개 Sprint(Sprint 0~4), 25개 Issue로 구성하고, RealWorld 공식 API 스펙 18개 엔드포인트와 9개 프론트엔드 라우트를 100% 커버한다. 기능 요구사항 R-F-01~R-F-19, 비기능 요구사항 R-N-01~R-N-06, 화면 기능 F-01~F-10 전체가 추적성 매트릭스에 매핑된다.

## 1. 스프린트 일람

| Sprint | 기간 | 목표(Outcome) | 주요 R-ID/F-ID | 이슈 수 |
|---|---|---|---|---|
| Sprint 0 | 1주 (Day 1~5) | 프로젝트 부트스트랩 -- BE/FE 스캐폴딩, 보안 인프라, DB 스키마, App Shell 완성 | R-N-01~R-N-04 | 5 |
| Sprint 1 | 1주 (Day 6~10) | 인증/사용자 모듈 -- 회원가입, 로그인, 현재 사용자 조회, 사용자 정보 수정, 인증 통합 테스트 | R-F-01~R-F-04, F-01~F-03 | 4 |
| Sprint 2 | 1.5주 (Day 11~17) | 아티클/태그 모듈 -- CRUD, 피드, 태그, 에디터, 아티클 상세, 홈 피드 화면 | R-F-08~R-F-13, R-F-19, R-N-05, R-N-06, F-05~F-07, F-10 | 6 |
| Sprint 3 | 1.5주 (Day 18~24) | 소셜 기능 -- 프로필, 팔로우, 댓글, 즐겨찾기 BE/FE | R-F-05~R-F-07, R-F-14~R-F-18, F-04, F-08, F-09 | 6 |
| Sprint 4 | 1주 (Day 25~29) | 통합/마무리 -- API 통합 테스트, E2E, API 문서, 최종 점검 | 전체 R-ID/F-ID 통합 검증 | 4 |

## 2. 스프린트 상세

### Sprint 0

> **목표**: 프로젝트 부트스트랩 -- 빌드 환경, 보안, DB 스키마, FE App Shell을 구성하여 Sprint 1 이후 기능 개발이 즉시 가능하도록 한다.

##### Issue: be-scaffold

- **유형**: chore
- **영역**: backend
- **우선순위**: P0
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 빈 프로젝트 상태 When `./gradlew build` 실행 Then 빌드 성공 + Hexagonal Architecture 패키지 구조(adapter/in/web, adapter/out/persistence, application/port/in, application/port/out, domain) 생성 완료
- **Contract Before**: 빈 `backend/` 디렉토리
- **Contract After**: `backend/build.gradle.kts`, `settings.gradle.kts`, `gradlew`, Spring Boot 3.x main class, Hexagonal Architecture 패키지 구조, Spring Modulith 모듈 경계 설정 완료, `./gradlew build` 성공
- **DoD Checklist**:
  - [ ] `build.gradle.kts` -- Spring Boot 3.x, Java 24, 의존성(Spring Web, JPA, Security, Flyway, PostgreSQL, Springdoc, Modulith, Testcontainers) 선언
  - [ ] `settings.gradle.kts` -- 프로젝트 이름 설정
  - [ ] Gradle Wrapper(`gradlew`, `gradlew.bat`, `gradle/wrapper/`) 포함
  - [ ] Main Application class 작성
  - [ ] Hexagonal Architecture 패키지 구조 생성 (user, article, comment, tag, profile 모듈별)
  - [ ] `./gradlew build` 성공
  - [ ] `./gradlew test` 성공 (컴파일 테스트)

##### Issue: fe-scaffold

- **유형**: chore
- **영역**: frontend
- **우선순위**: P0
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 빈 프로젝트 상태 When `pnpm dev` 실행 Then Vite dev server 기동 + React 18 Welcome 페이지 렌더링
- **Contract Before**: 빈 `frontend/` 디렉토리
- **Contract After**: `frontend/package.json`, `vite.config.ts`, `tsconfig.json`, React 18 + TypeScript 진입점, CSS Modules 설정, Bootstrap 4 CDN link, `pnpm dev` 기동 가능
- **DoD Checklist**:
  - [ ] `package.json` -- React 18, TypeScript, Vite, react-router-dom 의존성
  - [ ] `vite.config.ts` -- proxy 설정(`/api` -> `http://localhost:8080`)
  - [ ] `tsconfig.json` -- strict mode, path alias
  - [ ] `src/main.tsx` 진입점 + `src/App.tsx` 루트 컴포넌트
  - [ ] CSS Modules 설정 확인 (Vite 기본 지원)
  - [ ] `index.html` -- Bootstrap 4 CDN link, Google Fonts, Ionicons CDN
  - [ ] `pnpm install && pnpm dev` 성공
  - [ ] `pnpm build` 성공 (TypeScript 컴파일 에러 없음)

##### Issue: be-shared-security

- **유형**: feature
- **영역**: backend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given Spring Boot 앱 기동 상태 When 인증 필요 엔드포인트에 유효한 JWT로 요청 Then 200 응답 + CORS 헤더 포함, When 유효하지 않은 JWT로 요청 Then 401 + 표준 에러 형식 응답
- **Contract Before**: `be-scaffold` 완료 -- Spring Boot 앱 기동 가능, 보안 설정 없음
- **Contract After**: Spring Security filter chain(JWT 검증), `JwtTokenProvider`(토큰 생성/파싱), CORS 설정(`localhost:5173` 허용), `GlobalExceptionHandler`(422/401/403/404 표준 에러 형식), Springdoc OpenAPI 기본 설정, `application.yml` 프로파일별 설정
- **DoD Checklist**:
  - [ ] `SecurityConfig` -- SecurityFilterChain 빈, CSRF 비활성, stateless session, 공개/인증 경로 분리
  - [ ] `JwtTokenProvider` -- HS256 서명, 토큰 생성(userId, email), 토큰 파싱/검증
  - [ ] `JwtAuthenticationFilter` -- OncePerRequestFilter, `Authorization: Token xxx` 헤더 파싱
  - [ ] `CorsConfig` -- `localhost:5173` origin 허용, GET/POST/PUT/DELETE/OPTIONS
  - [ ] `GlobalExceptionHandler` -- `@RestControllerAdvice`, 422/401/403/404 표준 에러 형식 `{"errors":{"field":["msg"]}}`
  - [ ] `application.yml` -- dev/stg/prod 프로파일, JWT secret, DB 접속 정보
  - [ ] Springdoc OpenAPI 기본 설정 (`/swagger-ui.html` 접근 확인)
  - [ ] 단위 테스트: JWT 생성/파싱/만료/변조 검증
  - [ ] Content-Type `application/json; charset=utf-8` 기본 설정
  - [ ] R-N-01(API 응답 형식), R-N-02(에러 응답 형식), R-N-03(JWT 인증), R-N-04(CORS) 충족

##### Issue: be-db-init

- **유형**: chore
- **영역**: backend
- **우선순위**: P0
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given PostgreSQL 로컬 인스턴스 실행 중 When Spring Boot 앱 기동 Then Flyway 마이그레이션 실행 + 7개 테이블(users, articles, comments, tags, article_tags, follows, favorites) 생성 완료
- **Contract Before**: `be-scaffold` + `be-shared-security` 완료 -- Spring Boot 앱 기동 가능, DB 테이블 없음
- **Contract After**: `V1__init_schema.sql` -- users, articles, comments, tags, article_tags, follows, favorites 테이블 + 인덱스 + 외래키, JPA Entity 매핑 기본 클래스, Testcontainers PostgreSQL 설정
- **DoD Checklist**:
  - [ ] `V1__init_schema.sql` -- 7개 테이블 DDL (users, articles, comments, tags, article_tags, follows, favorites)
  - [ ] 인덱스: `articles.slug` unique, `users.email` unique, `users.username` unique, `articles.created_at` desc
  - [ ] 외래키: articles.author_id -> users.id, comments.article_id -> articles.id, comments.author_id -> users.id 등
  - [ ] JPA `@Entity` 기본 매핑 클래스 (BaseEntity with id, createdAt, updatedAt)
  - [ ] Testcontainers PostgreSQL 설정 (`@TestConfiguration`)
  - [ ] `./gradlew bootRun` 시 Flyway 마이그레이션 자동 실행 확인
  - [ ] `./gradlew test` 시 Testcontainers로 마이그레이션 검증

##### Issue: fe-app-shell

- **유형**: feature
- **영역**: frontend
- **우선순위**: P0
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given FE dev server 기동 상태 When 브라우저에서 `/#/` 접근 Then Header(네비게이션) + Footer + 빈 콘텐츠 영역 렌더링, When 로그인 상태 Then 네비게이션에 "New Article", "Settings", 사용자명 표시
- **Contract Before**: `fe-scaffold` 완료 -- Vite + React 기동 가능, 라우터/레이아웃 없음
- **Contract After**: React Router 설정(9개 라우트), Header 컴포넌트(인증 상태별 네비게이션), Footer 컴포넌트, API client(`axios` 인스턴스, JWT interceptor), Auth context(JWT localStorage 관리)
- **DoD Checklist**:
  - [ ] React Router -- HashRouter, 9개 라우트(`/`, `/login`, `/register`, `/settings`, `/editor`, `/editor/:slug`, `/article/:slug`, `/profile/:username`, `/profile/:username/favorites`)
  - [ ] `Header` 컴포넌트 -- 비인증(Home, Sign in, Sign up) / 인증(Home, New Article, Settings, Username) 네비게이션
  - [ ] `Footer` 컴포넌트 -- RealWorld 표준 푸터
  - [ ] API client -- axios 인스턴스, baseURL `/api`, JWT `Authorization: Token xxx` interceptor
  - [ ] Auth context -- JWT localStorage 저장/조회/삭제, 로그인 상태 관리
  - [ ] CSS Modules 기본 스타일 적용 (Bootstrap 4 CDN 스타일 위에 커스텀)
  - [ ] `pnpm build` 성공

### Sprint 1

> **목표**: 인증/사용자 모듈 -- 회원가입, 로그인, 현재 사용자 조회, 사용자 정보 수정을 BE/FE 모두 구현하여 인증 플로우를 완성한다.

##### Issue: be-user-auth

- **유형**: feature
- **영역**: backend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given Spring Boot 앱 기동 상태 When POST /api/users with 유효 데이터 Then 200 + User 객체(JWT 포함), When POST /api/users/login with 올바른 credentials Then 200 + User 객체, When GET /api/user with 유효 JWT Then 200 + 현재 User, When PUT /api/user with 수정 데이터 Then 200 + 갱신된 User
- **Contract Before**: Sprint 0 완료 -- DB 스키마, Security, JWT 인프라 존재, User 도메인 로직 없음
- **Contract After**: User 도메인(Entity, Value Objects), UserService(회원가입, 로그인, 조회, 수정), UserController(4개 엔드포인트), UserRepository, BCrypt 비밀번호 해싱, 단위/통합 테스트
- **DoD Checklist**:
  - [ ] User 도메인 모델 -- `User` Entity, `Email`, `Username` Value Object
  - [ ] `RegisterUserUseCase` + `LoginUseCase` + `GetCurrentUserUseCase` + `UpdateUserUseCase` 포트/서비스
  - [ ] `UserController` -- `POST /api/users`, `POST /api/users/login`, `GET /api/user`, `PUT /api/user`
  - [ ] `UserRepository` (JPA) -- findByEmail, findByUsername, existsByEmail, existsByUsername
  - [ ] BCrypt 비밀번호 해싱
  - [ ] 중복 email/username 검증 -> 422 에러
  - [ ] 단위 테스트: 도메인 로직, 서비스 레이어
  - [ ] 통합 테스트: MockMvc + Testcontainers (정상/실패 시나리오)
  - [ ] R-F-01(회원가입), R-F-02(로그인), R-F-03(현재 사용자 조회), R-F-04(사용자 정보 수정) 충족

##### Issue: fe-auth-pages

- **유형**: feature
- **영역**: frontend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given 브라우저에서 `/#/login` 접근 When 올바른 email/password 입력 후 "Sign in" 클릭 Then JWT localStorage 저장 + 홈 리다이렉트 + 네비게이션 인증 상태 변경, Given `/#/register` 접근 When 유효 데이터 입력 후 "Sign up" 클릭 Then 계정 생성 + JWT 저장 + 홈 리다이렉트
- **Contract Before**: `fe-app-shell` 완료 -- 라우터, Header, API client 존재, 로그인/회원가입 페이지 없음
- **Contract After**: Login 페이지 컴포넌트(`/#/login`), Register 페이지 컴포넌트(`/#/register`), 폼 검증, API 호출, JWT localStorage 관리, 에러 메시지 표시, CSS Modules 스타일
- **DoD Checklist**:
  - [ ] `LoginPage` -- email/password 폼, "Sign in" 버튼, 에러 메시지 표시, "Need an account?" 링크
  - [ ] `RegisterPage` -- username/email/password 폼, "Sign up" 버튼, 에러 메시지 표시, "Have an account?" 링크
  - [ ] API 연동: `POST /api/users/login`, `POST /api/users`
  - [ ] JWT localStorage 저장 + Auth context 업데이트
  - [ ] 로그인 성공 시 홈(`/#/`) 리다이렉트
  - [ ] 서버 에러(422) 메시지 리스트 렌더링
  - [ ] CSS Modules 스타일 적용 (Bootstrap 4 테마 기반)
  - [ ] F-01(회원가입), F-02(로그인/로그아웃) 충족

##### Issue: fe-settings-page

- **유형**: feature
- **영역**: frontend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 로그인 상태에서 `/#/settings` 접근 When 프로필 필드 수정 후 "Update Settings" 클릭 Then 프로필 갱신 + 성공 피드백, When "Logout" 클릭 Then JWT 삭제 + 홈 리다이렉트
- **Contract Before**: `fe-auth-pages` 완료 -- 로그인/JWT 관리 동작, Settings 페이지 없음
- **Contract After**: Settings 페이지 컴포넌트(`/#/settings`), 프로필 수정 폼(image URL, username, bio, email, password), 로그아웃 버튼, API 연동(`PUT /api/user`), CSS Modules 스타일
- **DoD Checklist**:
  - [ ] `SettingsPage` -- image URL, username, bio(textarea), email, new password 폼
  - [ ] 현재 사용자 데이터 폼에 프리로드 (`GET /api/user`)
  - [ ] "Update Settings" -> `PUT /api/user` API 호출
  - [ ] "Or click here to logout" 버튼 -> JWT 삭제 + Auth context 초기화 + 홈 리다이렉트
  - [ ] 서버 에러(422) 메시지 표시
  - [ ] CSS Modules 스타일 적용
  - [ ] F-03(프로필 조회/수정) 충족

##### Issue: integration-auth

- **유형**: test
- **영역**: backend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given Testcontainers PostgreSQL 환경 When 인증 관련 전체 시나리오 실행 Then 회원가입 -> 로그인 -> 현재 사용자 조회 -> 사용자 정보 수정 플로우 성공 + 에러 케이스(중복 email, 잘못된 password, 만료 JWT) 검증
- **Contract Before**: `be-user-auth` 완료 -- User 4개 엔드포인트 동작, 통합 테스트 기본 존재
- **Contract After**: 인증 플로우 통합 테스트 스위트 -- 정상 플로우(가입->로그인->조회->수정), 에러 케이스(중복 email/username, 잘못된 credentials, JWT 누락/변조), MockMvc + Testcontainers
- **DoD Checklist**:
  - [ ] 정상 플로우 테스트: 가입 -> 로그인 -> GET /api/user -> PUT /api/user 순차 검증
  - [ ] 에러 케이스: 중복 email -> 422, 중복 username -> 422
  - [ ] 에러 케이스: 잘못된 password -> 401/422
  - [ ] 에러 케이스: JWT 누락 -> 401, 변조 JWT -> 401
  - [ ] 에러 응답 형식 검증: `{"errors":{"field":["msg"]}}` 구조
  - [ ] R-N-01~R-N-04 관련 통합 검증 (Content-Type, 에러 형식, JWT, CORS)
  - [ ] `./gradlew test` 전체 통과

### Sprint 2

> **목표**: 아티클/태그 모듈 -- 아티클 CRUD, 목록/피드 조회, 태그 관리 BE + 에디터, 아티클 상세, 홈 피드 FE를 구현하여 콘텐츠 핵심 기능을 완성한다.

##### Issue: be-article-crud

- **유형**: feature
- **영역**: backend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given 로그인 상태 When POST /api/articles with 유효 데이터 Then 200 + Article 객체(slug 자동 생성), When GET /api/articles/:slug Then 200 + Article(body 포함), When PUT /api/articles/:slug by 작성자 Then 200 + 갱신된 Article, When DELETE /api/articles/:slug by 작성자 Then 200/204
- **Contract Before**: Sprint 1 완료 -- User/Auth 동작, Article 도메인 없음
- **Contract After**: Article 도메인(Entity, Slug VO), ArticleService(생성, 단건 조회, 수정, 삭제), ArticleController(4개 엔드포인트), ArticleRepository, Slug 자동 생성 로직, 작성자 권한 검증
- **DoD Checklist**:
  - [ ] Article 도메인 모델 -- `Article` Entity, `Slug` Value Object, `TagList`
  - [ ] `CreateArticleUseCase` + `GetArticleUseCase` + `UpdateArticleUseCase` + `DeleteArticleUseCase`
  - [ ] `ArticleController` -- `POST /api/articles`, `GET /api/articles/:slug`, `PUT /api/articles/:slug`, `DELETE /api/articles/:slug`
  - [ ] Slug 자동 생성: title -> lowercase-hyphenated, 중복 시 suffix 추가
  - [ ] 작성자 권한 검증: 수정/삭제 시 작성자 != 요청자 -> 403
  - [ ] 삭제 시 연관 댓글 cascade 삭제
  - [ ] 단위 테스트: Slug 생성, 권한 검증 도메인 로직
  - [ ] 통합 테스트: CRUD 전체 시나리오 (MockMvc + Testcontainers)
  - [ ] R-F-10(단건 조회), R-F-11(생성), R-F-12(수정), R-F-13(삭제), R-N-06(Slug 자동 생성) 충족

##### Issue: be-tag-module

- **유형**: feature
- **영역**: backend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 태그가 포함된 아티클 존재 When GET /api/tags Then 200 + tags[] (사용 중인 태그 목록), When 아티클 생성 시 tagList 전달 Then 아티클-태그 연관 관계 생성
- **Contract Before**: `be-article-crud` 완료 -- Article CRUD 동작, Tag 모듈 없음
- **Contract After**: Tag 도메인(Entity), TagService(태그 목록 조회, 아티클-태그 연관), TagController(`GET /api/tags`), Article 생성 시 tagList 처리 로직
- **DoD Checklist**:
  - [ ] Tag 도메인 모델 -- `Tag` Entity, `article_tags` 연관
  - [ ] `GetTagsUseCase` 포트/서비스
  - [ ] `TagController` -- `GET /api/tags`
  - [ ] Article 생성/수정 시 tagList 처리 (신규 태그 자동 생성, 기존 태그 연결)
  - [ ] 단위 테스트: 태그 생성/연관 로직
  - [ ] 통합 테스트: GET /api/tags, 아티클+태그 생성 시나리오
  - [ ] R-F-19(태그 목록 조회) 충족

##### Issue: be-article-feed

- **유형**: feature
- **영역**: backend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given 아티클 다수 존재 When GET /api/articles?tag=x&limit=10&offset=0 Then 200 + 필터링된 articles[] + articlesCount, When GET /api/articles/feed with 유효 JWT Then 200 + 팔로잉 사용자 아티클 목록
- **Contract Before**: `be-article-crud` + `be-tag-module` 완료 -- Article CRUD + Tag 존재, 목록/피드 없음
- **Contract After**: Article 목록 조회(tag/author/favorited 필터, limit/offset 페이지네이션), Feed 조회(팔로잉 사용자 아티클), QueryDSL/JPQL 동적 쿼리, 응답에 favorited/favoritesCount/author Profile 포함
- **DoD Checklist**:
  - [ ] `ListArticlesUseCase` -- tag, author, favorited 필터 + limit/offset 페이지네이션
  - [ ] `FeedArticlesUseCase` -- 팔로잉 사용자 아티클 조회 + limit/offset
  - [ ] `ArticleController` 확장 -- `GET /api/articles`, `GET /api/articles/feed`
  - [ ] 응답 형식: `{"articles": [...], "articlesCount": N}`, body 필드 미포함 (2024/08/16 스펙)
  - [ ] 각 아티클에 favorited, favoritesCount, author(Profile) 포함
  - [ ] 로그인 선택적: 비인증 시 favorited=false, 인증 시 실제 값
  - [ ] 단위 테스트: 필터/페이지네이션 로직
  - [ ] 통합 테스트: 다양한 필터 조합, 피드 시나리오
  - [ ] R-F-08(아티클 목록), R-F-09(피드), R-N-05(페이지네이션) 충족

##### Issue: fe-editor-page

- **유형**: feature
- **영역**: frontend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given 로그인 상태에서 `/#/editor` 접근 When title/description/body/tags 입력 후 "Publish Article" 클릭 Then 아티클 생성 + `/#/article/:slug` 리다이렉트, Given `/#/editor/:slug` 접근 When 기존 데이터 로드 + 수정 후 "Publish Article" 클릭 Then 아티클 수정 + 상세 페이지 이동
- **Contract Before**: `fe-app-shell` + Sprint 1 FE 완료 -- 라우터/Auth 동작, 에디터 페이지 없음
- **Contract After**: Editor 페이지 컴포넌트(`/#/editor`, `/#/editor/:slug`), 아티클 생성/수정 폼, 태그 입력(Enter키로 추가, X로 삭제), API 연동, CSS Modules 스타일
- **DoD Checklist**:
  - [ ] `EditorPage` -- title, description, body(textarea), tag input 폼
  - [ ] 신규 모드(`/#/editor`): 빈 폼 -> `POST /api/articles`
  - [ ] 편집 모드(`/#/editor/:slug`): 기존 데이터 로드(`GET /api/articles/:slug`) -> `PUT /api/articles/:slug`
  - [ ] 태그 입력: Enter 키로 태그 추가, 태그 옆 X 버튼으로 삭제
  - [ ] 성공 시 `/#/article/:slug` 리다이렉트
  - [ ] 서버 에러(422) 메시지 표시
  - [ ] CSS Modules 스타일 적용
  - [ ] F-07(아티클 CRUD) FE 측 충족

##### Issue: fe-article-page

- **유형**: feature
- **영역**: frontend
- **우선순위**: P0
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given `/#/article/:slug` 접근 When 아티클 존재 Then 배너(title, author, date, action buttons) + 본문(마크다운 렌더링) + 태그 리스트 표시, When 작성자 본인 Then Edit/Delete 버튼 표시
- **Contract Before**: `fe-editor-page` 진행 중/완료 -- 에디터 존재, 상세 페이지 없음
- **Contract After**: ArticlePage 컴포넌트(`/#/article/:slug`), 아티클 배너(메타 + 액션 버튼), 마크다운 렌더링(marked 라이브러리), 태그 리스트, 작성자 권한별 Edit/Delete 버튼, CSS Modules 스타일
- **DoD Checklist**:
  - [ ] `ArticlePage` -- `GET /api/articles/:slug` 데이터 로드
  - [ ] 배너: title, author 프로필(image, username, date), 작성자면 Edit/Delete 버튼, 비작성자면 Follow/Favorite 버튼
  - [ ] 본문: 마크다운 -> HTML 렌더링 (`marked` 또는 유사 라이브러리)
  - [ ] 태그 리스트: 아티클 하단 tagList 표시
  - [ ] Delete 클릭 -> `DELETE /api/articles/:slug` -> 홈 리다이렉트
  - [ ] Edit 클릭 -> `/#/editor/:slug` 이동
  - [ ] CSS Modules 스타일 적용
  - [ ] F-07(아티클 CRUD) 조회/삭제 FE 측 충족

##### Issue: fe-home-feed

- **유형**: feature
- **영역**: frontend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given 홈(`/#/`) 접근 When 비인증 상태 Then "Global Feed" 탭 + 아티클 카드 목록 + 사이드바 태그 + 페이지네이션, When 인증 상태 Then "Your Feed" 탭 추가, When 태그 클릭 Then 해당 태그 필터 탭 활성화
- **Contract Before**: `fe-article-page` 완료 -- 상세 페이지 존재, 홈 피드 없음
- **Contract After**: HomePage 컴포넌트(`/#/`), 피드 탭(Your Feed/Global Feed/Tag Filter), ArticlePreview 카드 컴포넌트, TagSidebar 컴포넌트, Pagination 컴포넌트, API 연동(articles, articles/feed, tags)
- **DoD Checklist**:
  - [ ] `HomePage` -- 배너 + 피드 영역 + 사이드바 2컬럼 레이아웃
  - [ ] 피드 탭: 비인증(Global Feed만) / 인증(Your Feed + Global Feed) / 태그 선택 시 태그 탭 추가
  - [ ] `ArticlePreview` 카드 -- author(image, username, date), title, description, "Read more...", tagList, favoritesCount
  - [ ] `TagSidebar` -- `GET /api/tags` -> 태그 클릭 시 태그 필터 탭 활성화
  - [ ] `Pagination` -- limit=10 기반 페이지 번호, offset 계산
  - [ ] Your Feed: `GET /api/articles/feed`, Global Feed: `GET /api/articles`, Tag: `GET /api/articles?tag=x`
  - [ ] CSS Modules 스타일 적용
  - [ ] F-05(글로벌 피드), F-06(개인 피드), F-10(태그 시스템) 충족

### Sprint 3

> **목표**: 소셜 기능 -- 프로필/팔로우, 댓글, 즐겨찾기 BE/FE를 구현하여 사용자 간 상호작용 기능을 완성한다.

##### Issue: be-profile-follow

- **유형**: feature
- **영역**: backend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 사용자 존재 When GET /api/profiles/:username Then 200 + Profile 객체, When POST /api/profiles/:username/follow Then 200 + Profile(following=true), When DELETE /api/profiles/:username/follow Then 200 + Profile(following=false)
- **Contract Before**: Sprint 1 User 모듈 완료 -- User CRUD 동작, Profile/Follow 엔드포인트 없음
- **Contract After**: ProfileController(프로필 조회, 팔로우, 언팔로우), FollowService, FollowRepository(follows 테이블 연동), Profile 응답 DTO(username, bio, image, following)
- **DoD Checklist**:
  - [ ] `GetProfileUseCase` + `FollowUserUseCase` + `UnfollowUserUseCase`
  - [ ] `ProfileController` -- `GET /api/profiles/:username`, `POST /api/profiles/:username/follow`, `DELETE /api/profiles/:username/follow`
  - [ ] Profile 응답: `{"profile": {"username", "bio", "image", "following"}}`
  - [ ] following 필드: 인증 시 실제 팔로우 상태, 비인증 시 false
  - [ ] 중복 팔로우 방지 (멱등성)
  - [ ] 단위 테스트: 팔로우/언팔로우 도메인 로직
  - [ ] 통합 테스트: 프로필 조회, 팔로우/언팔로우 시나리오
  - [ ] R-F-05(프로필 조회), R-F-06(팔로우), R-F-07(언팔로우) 충족

##### Issue: be-comment-module

- **유형**: feature
- **영역**: backend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 아티클 존재 + 로그인 상태 When POST /api/articles/:slug/comments Then 200 + Comment 객체, When GET /api/articles/:slug/comments Then 200 + comments[], When DELETE /api/articles/:slug/comments/:id by 작성자 Then 200/204
- **Contract Before**: Sprint 2 Article 모듈 완료 -- Article CRUD 동작, Comment 엔드포인트 없음
- **Contract After**: Comment 도메인(Entity), CommentService(추가, 목록, 삭제), CommentController(3개 엔드포인트), CommentRepository, 작성자 권한 검증
- **DoD Checklist**:
  - [ ] Comment 도메인 모델 -- `Comment` Entity (id, body, article, author, createdAt, updatedAt)
  - [ ] `AddCommentUseCase` + `ListCommentsUseCase` + `DeleteCommentUseCase`
  - [ ] `CommentController` -- `POST /api/articles/:slug/comments`, `GET /api/articles/:slug/comments`, `DELETE /api/articles/:slug/comments/:id`
  - [ ] 댓글 삭제 권한: 작성자 != 요청자 -> 403
  - [ ] 댓글 응답에 author Profile 포함
  - [ ] 단위 테스트: 권한 검증 로직
  - [ ] 통합 테스트: 댓글 CRUD + 권한 에러 시나리오
  - [ ] R-F-14(댓글 추가), R-F-15(댓글 목록), R-F-16(댓글 삭제) 충족

##### Issue: be-favorite

- **유형**: feature
- **영역**: backend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 로그인 상태 + 아티클 존재 When POST /api/articles/:slug/favorite Then 200 + Article(favorited=true, favoritesCount+1), When DELETE /api/articles/:slug/favorite Then 200 + Article(favorited=false, favoritesCount-1)
- **Contract Before**: Sprint 2 Article 모듈 완료 -- Article CRUD + 목록/피드 동작, Favorite 엔드포인트 없음
- **Contract After**: FavoriteService(즐겨찾기 추가/제거), ArticleController 확장(favorite/unfavorite 엔드포인트), FavoriteRepository(favorites 테이블 연동)
- **DoD Checklist**:
  - [ ] `FavoriteArticleUseCase` + `UnfavoriteArticleUseCase`
  - [ ] `ArticleController` 확장 -- `POST /api/articles/:slug/favorite`, `DELETE /api/articles/:slug/favorite`
  - [ ] 응답: Article 전체 객체 (favorited, favoritesCount 갱신)
  - [ ] 중복 즐겨찾기 방지 (멱등성)
  - [ ] 단위 테스트: 즐겨찾기/해제 로직, 카운트 검증
  - [ ] 통합 테스트: 즐겨찾기 추가/해제, 이미 즐겨찾기된 상태에서 재호출
  - [ ] R-F-17(즐겨찾기), R-F-18(즐겨찾기 해제) 충족

##### Issue: fe-profile-page

- **유형**: feature
- **영역**: frontend
- **우선순위**: P1
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given `/#/profile/:username` 접근 When 프로필 존재 Then 사용자 정보(image, username, bio) + "My Articles"/"Favorited Articles" 탭 + 아티클 목록, When 타인 프로필 + 로그인 상태 Then Follow/Unfollow 버튼 표시 + 클릭 시 토글
- **Contract Before**: Sprint 1~2 FE 완료 -- Auth, ArticlePreview 존재, Profile 페이지 없음
- **Contract After**: ProfilePage 컴포넌트(`/#/profile/:username`, `/#/profile/:username/favorites`), 사용자 배너(image, username, bio, Follow/Edit Settings 버튼), 탭(My Articles/Favorited Articles), 아티클 목록 재사용
- **DoD Checklist**:
  - [ ] `ProfilePage` -- `GET /api/profiles/:username` 프로필 데이터 로드
  - [ ] 사용자 배너: image, username, bio
  - [ ] 본인 프로필: "Edit Profile Settings" 버튼 -> `/#/settings`
  - [ ] 타인 프로필 + 인증: Follow/Unfollow 버튼 (`POST/DELETE /api/profiles/:username/follow`)
  - [ ] 탭: "My Articles" (`GET /api/articles?author=username`) / "Favorited Articles" (`GET /api/articles?favorited=username`)
  - [ ] `ArticlePreview` 컴포넌트 재사용 + 페이지네이션
  - [ ] CSS Modules 스타일 적용
  - [ ] F-04(팔로우/언팔로우) FE 충족

##### Issue: fe-comment-section

- **유형**: feature
- **영역**: frontend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given `/#/article/:slug` 아티클 상세 페이지 When 로그인 상태 Then 댓글 입력 폼 + 댓글 목록 표시, When "Post Comment" 클릭 Then 댓글 추가 + 목록 갱신, When 자기 댓글의 삭제 아이콘 클릭 Then 댓글 삭제
- **Contract Before**: `fe-article-page` 완료 -- 아티클 상세 페이지 존재, 댓글 섹션 없음
- **Contract After**: CommentSection 컴포넌트(댓글 폼 + 댓글 목록), CommentCard 컴포넌트(author, date, body, 삭제 버튼), API 연동(comments CRUD)
- **DoD Checklist**:
  - [ ] `CommentSection` -- `GET /api/articles/:slug/comments` 댓글 목록 로드
  - [ ] 댓글 입력 폼: body textarea + author image + "Post Comment" 버튼 (인증 시만 표시)
  - [ ] `CommentCard` -- author(image, username, date), body, 자기 댓글이면 삭제(trash) 아이콘
  - [ ] 댓글 추가: `POST /api/articles/:slug/comments` -> 목록 갱신
  - [ ] 댓글 삭제: `DELETE /api/articles/:slug/comments/:id` -> 목록에서 제거
  - [ ] 비인증 시: "Sign in or sign up to add comments" 링크
  - [ ] CSS Modules 스타일 적용
  - [ ] F-08(댓글) FE 충족

##### Issue: fe-favorite-button

- **유형**: feature
- **영역**: frontend
- **우선순위**: P1
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 아티클 카드 또는 상세 페이지 When 로그인 상태에서 하트 버튼 클릭 Then favorited 토글 + favoritesCount 즉시 갱신(optimistic update)
- **Contract Before**: `fe-home-feed` + `fe-article-page` 완료 -- ArticlePreview/ArticlePage 존재, 즐겨찾기 버튼 미구현
- **Contract After**: FavoriteButton 컴포넌트(하트 아이콘 + 카운트), optimistic update 로직, API 연동(favorite/unfavorite), ArticlePreview/ArticlePage에 통합
- **DoD Checklist**:
  - [ ] `FavoriteButton` -- 하트 아이콘 + favoritesCount 표시
  - [ ] favorited=true: 활성 스타일(filled heart), false: 비활성 스타일(outline heart)
  - [ ] 클릭 시 optimistic update: UI 즉시 갱신 -> API 호출 -> 실패 시 롤백
  - [ ] API: `POST /api/articles/:slug/favorite` / `DELETE /api/articles/:slug/favorite`
  - [ ] `ArticlePreview` (홈 피드 카드)에 통합
  - [ ] `ArticlePage` (상세 배너)에 통합
  - [ ] 비인증 시 클릭 -> `/#/login` 리다이렉트
  - [ ] CSS Modules 스타일 적용
  - [ ] F-09(즐겨찾기) FE 충족

### Sprint 4

> **목표**: 통합/마무리 -- 전체 API 통합 테스트, E2E 테스트, API 문서화, 최종 점검을 수행하여 RealWorld 스펙 100% 준수를 검증한다.

##### Issue: integration-api

- **유형**: test
- **영역**: backend
- **우선순위**: P0
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given 전체 BE 모듈 완료 상태 When 통합 테스트 스위트 실행 Then 18개 API 엔드포인트 전체 정상/에러 시나리오 통과
- **Contract Before**: Sprint 0~3 BE 완료 -- 18개 엔드포인트 동작, 모듈별 통합 테스트 존재
- **Contract After**: 전체 API 통합 테스트 스위트 -- 18개 엔드포인트별 정상/에러 케이스, 크로스-모듈 시나리오(가입->글쓰기->댓글->즐겨찾기->팔로우->피드 검증), RealWorld Postman Collection 기반 시나리오
- **DoD Checklist**:
  - [ ] User 엔드포인트 (4): register, login, get current, update
  - [ ] Profile 엔드포인트 (3): get profile, follow, unfollow
  - [ ] Article 엔드포인트 (6): create, get single, update, delete, list, feed
  - [ ] Comment 엔드포인트 (3): add, list, delete
  - [ ] Favorite 엔드포인트 (2): favorite, unfavorite
  - [ ] Tags 엔드포인트 (1): list tags
  - [ ] 크로스-모듈 시나리오: 가입 -> 글쓰기(태그) -> 댓글 -> 즐겨찾기 -> 타인 가입 -> 팔로우 -> 피드 검증
  - [ ] 에러 케이스: 401, 403, 404, 422 각각 검증
  - [ ] `./gradlew test` 전체 통과

##### Issue: e2e-tests

- **유형**: test
- **영역**: frontend
- **우선순위**: P1
- **Estimated Effort**: 2d
- **Acceptance Criteria**: Given FE+BE 로컬 기동 상태 When Playwright E2E 테스트 실행 Then 골든 패스(가입 -> 글쓰기 -> 글 조회 -> 댓글 -> 즐겨찾기 -> 프로필 확인) 통과
- **Contract Before**: Sprint 0~3 FE/BE 전체 완료 -- 모든 페이지/API 동작, E2E 테스트 없음
- **Contract After**: Playwright E2E 테스트 스위트 -- 골든 패스(register -> write article -> read -> comment -> favorite -> profile), 인증 플로우, 네비게이션 검증
- **DoD Checklist**:
  - [ ] Playwright 설정: `playwright.config.ts`, 브라우저(chromium), baseURL `http://localhost:5173`
  - [ ] 골든 패스 1: 회원가입 -> 홈 리다이렉트 -> 네비게이션 변경 확인
  - [ ] 골든 패스 2: 아티클 작성 -> 상세 페이지 -> 마크다운 렌더링 확인
  - [ ] 골든 패스 3: 댓글 작성 -> 댓글 목록 표시
  - [ ] 골든 패스 4: 즐겨찾기 -> 카운트 증가 -> 프로필 Favorited 탭 확인
  - [ ] 에러 시나리오: 잘못된 로그인 -> 에러 메시지 표시
  - [ ] `pnpm playwright test` 통과

##### Issue: api-docs

- **유형**: docs
- **영역**: backend
- **우선순위**: P2
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given Spring Boot 앱 기동 상태 When `http://localhost:8080/swagger-ui.html` 접근 Then 18개 엔드포인트 전체 문서화 + 요청/응답 스키마 + 인증 정보 표시
- **Contract Before**: Sprint 0~3 BE 완료 + `be-shared-security`의 Springdoc 기본 설정 존재, 상세 API 문서 없음
- **Contract After**: Springdoc OpenAPI 어노테이션(@Operation, @ApiResponse, @Schema) 전체 적용, Swagger UI에서 18개 엔드포인트 확인 + Try It Out 가능
- **DoD Checklist**:
  - [ ] 각 Controller 메서드에 `@Operation(summary, description)` 추가
  - [ ] 요청 DTO에 `@Schema(description, example)` 추가
  - [ ] 응답 DTO에 `@Schema` 추가
  - [ ] 에러 응답에 `@ApiResponse(responseCode, description)` 추가
  - [ ] JWT 인증 스키마 설정 (`@SecurityScheme`)
  - [ ] Swagger UI 접근 + 전체 엔드포인트 렌더링 확인
  - [ ] Try It Out으로 실제 API 호출 가능 확인

##### Issue: final-polish

- **유형**: chore
- **영역**: backend
- **우선순위**: P2
- **Estimated Effort**: 1d
- **Acceptance Criteria**: Given 전체 구현 완료 상태 When 크로스커팅 점검 수행 Then 로깅 일관성, 에러 메시지 통일, README 작성, smoke test 통과
- **Contract Before**: Sprint 0~4 전체 완료, 크로스커팅 점검 미수행
- **Contract After**: 로깅 레벨/형식 통일, 에러 메시지 일관성, README.md(프로젝트 소개, 기술 스택, 로컬 실행 가이드), LOCAL.md 갱신, smoke test(FE+BE 동시 기동 + 골든 패스 수동 확인)
- **DoD Checklist**:
  - [ ] 로깅: 각 모듈 SLF4J 로거, INFO/WARN/ERROR 레벨 적절 배치
  - [ ] 에러 메시지: RealWorld 스펙 일관 형식 (`{"errors":{"body":["can't be blank"]}}`)
  - [ ] README.md: 프로젝트 소개, 기술 스택, 아키텍처, 로컬 실행 가이드
  - [ ] LOCAL.md: dev/stg/prod 프로파일별 부팅 명령 최신화
  - [ ] `.env.dev.example`, `.env.stg.example`, `.env.prod.example` 확인
  - [ ] FE + BE 동시 기동 smoke test
  - [ ] `./gradlew build` + `pnpm build` 최종 성공

## 3. 의존성 그래프

```
Sprint 0 (Foundation)
  be-scaffold ─────────> be-shared-security ───> be-db-init
  fe-scaffold ─────────> fe-app-shell
                              │
Sprint 1 (Auth)               │
  be-db-init ──────────> be-user-auth ─────────> integration-auth
  fe-app-shell ────────> fe-auth-pages ────────> fe-settings-page
                              │
Sprint 2 (Article/Tag)        │
  be-user-auth ────────> be-article-crud ──────> be-tag-module ──> be-article-feed
  fe-auth-pages ───────> fe-editor-page ───────> fe-article-page ──> fe-home-feed
                              │
Sprint 3 (Social)             │
  be-user-auth ────────> be-profile-follow
  be-article-crud ─────> be-comment-module
  be-article-crud ─────> be-favorite
  fe-home-feed ────────> fe-profile-page
  fe-article-page ─────> fe-comment-section
  fe-home-feed ────────> fe-favorite-button
                              │
Sprint 4 (Integration)        │
  All BE modules ──────> integration-api
  All FE/BE modules ───> e2e-tests
  All BE modules ──────> api-docs
  All modules ─────────> final-polish
```

상세 DAG (이슈 번호 기반):

```
[1] be-scaffold
 └──> [3] be-shared-security
       └──> [4] be-db-init
             └──> [6] be-user-auth
                   ├──> [9] integration-auth
                   ├──> [10] be-article-crud
                   │     ├──> [11] be-tag-module
                   │     │     └──> [12] be-article-feed
                   │     ├──> [17] be-comment-module
                   │     └──> [18] be-favorite
                   └──> [16] be-profile-follow

[2] fe-scaffold
 └──> [5] fe-app-shell
       └──> [7] fe-auth-pages
             ├──> [8] fe-settings-page
             └──> [13] fe-editor-page
                   └──> [14] fe-article-page
                         ├──> [20] fe-comment-section
                         └──> [15] fe-home-feed
                               ├──> [19] fe-profile-page
                               └──> [21] fe-favorite-button

[22] integration-api  <── [6,10,11,12,16,17,18]
[23] e2e-tests        <── [All FE + BE]
[24] api-docs         <── [All BE controllers]
[25] final-polish     <── [All modules]
```

## 4. 추적성 매트릭스

| R-ID | F-ID | Sprint | Issue Slug |
|---|---|---|---|
| R-F-01 | F-01 | Sprint 1 | be-user-auth, fe-auth-pages |
| R-F-02 | F-02 | Sprint 1 | be-user-auth, fe-auth-pages |
| R-F-03 | F-02 | Sprint 1 | be-user-auth, fe-auth-pages |
| R-F-04 | F-03 | Sprint 1 | be-user-auth, fe-settings-page |
| R-F-05 | F-04 | Sprint 3 | be-profile-follow, fe-profile-page |
| R-F-06 | F-04 | Sprint 3 | be-profile-follow, fe-profile-page |
| R-F-07 | F-04 | Sprint 3 | be-profile-follow, fe-profile-page |
| R-F-08 | F-05 | Sprint 2 | be-article-feed, fe-home-feed |
| R-F-09 | F-06 | Sprint 2 | be-article-feed, fe-home-feed |
| R-F-10 | F-07 | Sprint 2 | be-article-crud, fe-article-page |
| R-F-11 | F-07 | Sprint 2 | be-article-crud, fe-editor-page |
| R-F-12 | F-07 | Sprint 2 | be-article-crud, fe-editor-page |
| R-F-13 | F-07 | Sprint 2 | be-article-crud, fe-article-page |
| R-F-14 | F-08 | Sprint 3 | be-comment-module, fe-comment-section |
| R-F-15 | F-08 | Sprint 3 | be-comment-module, fe-comment-section |
| R-F-16 | F-08 | Sprint 3 | be-comment-module, fe-comment-section |
| R-F-17 | F-09 | Sprint 3 | be-favorite, fe-favorite-button |
| R-F-18 | F-09 | Sprint 3 | be-favorite, fe-favorite-button |
| R-F-19 | F-10 | Sprint 2 | be-tag-module, fe-home-feed |
| R-N-01 | - | Sprint 0 | be-shared-security |
| R-N-02 | - | Sprint 0 | be-shared-security |
| R-N-03 | - | Sprint 0 | be-shared-security |
| R-N-04 | - | Sprint 0 | be-shared-security |
| R-N-05 | F-05, F-06 | Sprint 2 | be-article-feed, fe-home-feed |
| R-N-06 | F-07 | Sprint 2 | be-article-crud |

## 5. 리스크 매핑

| 15-risk Risk-ID | 영향 받는 Sprint/Issue | 대응 이슈 |
|---|---|---|
| RISK-01 | Sprint 0 / be-scaffold, be-shared-security | be-scaffold (build.gradle.kts에서 호환 의존성 버전 명시, Java 24 preview 기능 비사용) |
| RISK-02 | Sprint 0 / be-db-init | be-db-init (Testcontainers PostgreSQL로 테스트 환경 분리, LOCAL.md에 Docker/PostgreSQL 설치 가이드) |
| RISK-03 | Sprint 0~2 / be-scaffold, be-user-auth, be-article-crud | be-scaffold (패키지 구조 탬플릿 + 11-coding-conventions 참조), integration-auth (구조 검증) |
| RISK-04 | Sprint 0 / be-scaffold | be-scaffold (Spring Modulith @ApplicationModule 최소 사용, 모듈 간 의존 방향 검증 테스트) |
| RISK-05 | Sprint 0~1 / be-shared-security, be-user-auth | be-shared-security (Authorization: Token 헤더 파싱 커스텀 필터), integration-auth (RealWorld 스펙 토큰 형식 검증) |
| RISK-06 | Sprint 0 / fe-scaffold, fe-app-shell | fe-scaffold (Bootstrap 4 CSS 로컬 번들 우선, CDN fallback), fe-app-shell (오프라인 대비 에러 처리) |
| RISK-07 | Sprint 0 / be-db-init | be-db-init (V1 단일 마이그레이션으로 전체 스키마, 추후 변경 시 V2 이후 순차 적용, Flyway 버전 관리 전략 문서화) |

## 6. 일정

```
Week 1 (Day 1~5):    Sprint 0 — Project Bootstrap
                      [be-scaffold][fe-scaffold][be-shared-security][be-db-init][fe-app-shell]

Week 2 (Day 6~10):   Sprint 1 — Authentication & User
                      [be-user-auth][fe-auth-pages][fe-settings-page][integration-auth]

Week 3 (Day 11~15):  Sprint 2 (전반) — Article & Tag (BE)
                      [be-article-crud][be-tag-module][be-article-feed][fe-editor-page]

Week 4 (Day 16~17):  Sprint 2 (후반) — Article & Tag (FE)
             (Day 18~22):  Sprint 3 (전반) — Social Features (BE + FE 시작)
                      [fe-article-page][fe-home-feed]
                      [be-profile-follow][be-comment-module][be-favorite][fe-profile-page]

Week 5 (Day 23~24):  Sprint 3 (후반) — Social Features (FE 완료)
             (Day 25~29):  Sprint 4 — Integration & Polish
                      [fe-comment-section][fe-favorite-button]
                      [integration-api][e2e-tests][api-docs][final-polish]
```

총 기간: 5~6주 (29 working days)

핵심 마일스톤:

| 마일스톤 | 시점 | 검증 기준 |
|---|---|---|
| M0: Foundation Ready | Day 5 완료 | BE/FE 빌드 성공, DB 마이그레이션, JWT 인증, App Shell |
| M1: Auth Complete | Day 10 완료 | 회원가입/로그인 E2E 동작, 통합 테스트 통과 |
| M2: Content Core | Day 17 완료 | 아티클 CRUD + 피드 + 태그 BE/FE 동작 |
| M3: Social Complete | Day 24 완료 | 프로필/팔로우/댓글/즐겨찾기 BE/FE 동작 |
| M4: Release Ready | Day 29 완료 | 전체 통합/E2E 테스트 통과, API 문서, smoke test |

## 7. sprint-bootstrap 입력

```yaml
sprints:
  - id: 0
    name: "Project Bootstrap"
    duration: "1 week"
    goal: "BE/FE scaffolding, security infra, DB schema, App Shell"
    issues:
      - slug: be-scaffold
        type: chore
        area: backend
        priority: P0
        effort: 1d
        r_ids: []
        f_ids: []
      - slug: fe-scaffold
        type: chore
        area: frontend
        priority: P0
        effort: 1d
        r_ids: []
        f_ids: []
      - slug: be-shared-security
        type: feature
        area: backend
        priority: P0
        effort: 2d
        r_ids: [R-N-01, R-N-02, R-N-03, R-N-04]
        f_ids: []
      - slug: be-db-init
        type: chore
        area: backend
        priority: P0
        effort: 1d
        r_ids: []
        f_ids: []
      - slug: fe-app-shell
        type: feature
        area: frontend
        priority: P0
        effort: 1d
        r_ids: []
        f_ids: []

  - id: 1
    name: "Authentication & User"
    duration: "1 week"
    goal: "User registration, login, profile management, auth integration tests"
    issues:
      - slug: be-user-auth
        type: feature
        area: backend
        priority: P0
        effort: 2d
        r_ids: [R-F-01, R-F-02, R-F-03, R-F-04]
        f_ids: []
      - slug: fe-auth-pages
        type: feature
        area: frontend
        priority: P0
        effort: 2d
        r_ids: []
        f_ids: [F-01, F-02]
      - slug: fe-settings-page
        type: feature
        area: frontend
        priority: P1
        effort: 1d
        r_ids: []
        f_ids: [F-03]
      - slug: integration-auth
        type: test
        area: backend
        priority: P1
        effort: 1d
        r_ids: []
        f_ids: []

  - id: 2
    name: "Article & Tag"
    duration: "1.5 weeks"
    goal: "Article CRUD, feed, tags, editor, article detail, home feed"
    issues:
      - slug: be-article-crud
        type: feature
        area: backend
        priority: P0
        effort: 2d
        r_ids: [R-F-10, R-F-11, R-F-12, R-F-13, R-N-06]
        f_ids: []
      - slug: be-tag-module
        type: feature
        area: backend
        priority: P1
        effort: 1d
        r_ids: [R-F-19]
        f_ids: []
      - slug: be-article-feed
        type: feature
        area: backend
        priority: P0
        effort: 2d
        r_ids: [R-F-08, R-F-09, R-N-05]
        f_ids: []
      - slug: fe-editor-page
        type: feature
        area: frontend
        priority: P0
        effort: 2d
        r_ids: []
        f_ids: [F-07]
      - slug: fe-article-page
        type: feature
        area: frontend
        priority: P0
        effort: 1d
        r_ids: []
        f_ids: [F-07]
      - slug: fe-home-feed
        type: feature
        area: frontend
        priority: P0
        effort: 2d
        r_ids: []
        f_ids: [F-05, F-06, F-10]

  - id: 3
    name: "Social Features"
    duration: "1.5 weeks"
    goal: "Profile, follow, comments, favorites - BE and FE"
    issues:
      - slug: be-profile-follow
        type: feature
        area: backend
        priority: P1
        effort: 1d
        r_ids: [R-F-05, R-F-06, R-F-07]
        f_ids: []
      - slug: be-comment-module
        type: feature
        area: backend
        priority: P1
        effort: 1d
        r_ids: [R-F-14, R-F-15, R-F-16]
        f_ids: []
      - slug: be-favorite
        type: feature
        area: backend
        priority: P1
        effort: 1d
        r_ids: [R-F-17, R-F-18]
        f_ids: []
      - slug: fe-profile-page
        type: feature
        area: frontend
        priority: P1
        effort: 2d
        r_ids: []
        f_ids: [F-04]
      - slug: fe-comment-section
        type: feature
        area: frontend
        priority: P1
        effort: 1d
        r_ids: []
        f_ids: [F-08]
      - slug: fe-favorite-button
        type: feature
        area: frontend
        priority: P1
        effort: 1d
        r_ids: []
        f_ids: [F-09]

  - id: 4
    name: "Integration & Polish"
    duration: "1 week"
    goal: "Full API integration tests, E2E tests, API docs, final polish"
    issues:
      - slug: integration-api
        type: test
        area: backend
        priority: P0
        effort: 2d
        r_ids: []
        f_ids: []
      - slug: e2e-tests
        type: test
        area: frontend
        priority: P1
        effort: 2d
        r_ids: []
        f_ids: []
      - slug: api-docs
        type: docs
        area: backend
        priority: P2
        effort: 1d
        r_ids: []
        f_ids: []
      - slug: final-polish
        type: chore
        area: backend
        priority: P2
        effort: 1d
        r_ids: []
        f_ids: []

project:
  name: "Conduit (RealWorld)"
  repo: "board-playground"
  tech_stack:
    backend: "Java 24 + Spring Boot 3 + Hexagonal Architecture + DDD + Spring Modulith + PostgreSQL + Flyway"
    frontend: "React 18 + TypeScript + Vite + CSS Modules + Bootstrap 4 CDN"
  monorepo_layout:
    backend: "backend/"
    frontend: "frontend/"
  build_commands:
    backend_build: "./gradlew build"
    backend_run: "./gradlew bootRun"
    frontend_install: "pnpm install"
    frontend_dev: "pnpm dev"
    frontend_build: "pnpm build"
  total_issues: 25
  total_effort: "33.5d"
  estimated_duration: "5-6 weeks (29 working days)"
```

## 8. Open Questions

- Playwright E2E 테스트에서 BE fixture 데이터 시딩 전략 (API 호출 vs DB 직접 삽입)
- Spring Modulith 모듈 간 이벤트 통신 범위 (현 단계 최소 사용 vs 적극 활용)
- Article slug 중복 시 suffix 전략 (UUID suffix vs 숫자 증가)
- JWT 만료 시간 정책 (개발 편의 긴 만료 vs RealWorld 스펙 미명시)
- CSS Modules와 Bootstrap 4 CDN 병행 시 스타일 우선순위 관리 전략
