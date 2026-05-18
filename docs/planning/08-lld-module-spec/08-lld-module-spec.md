---
doc_type: module-spec
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

# Conduit (RealWorld) — Module Spec (LLD — 모듈/통신)

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (architect) | 초안 -- BE 5개 서비스 모듈 + FE 4개 페이지 모듈 정의 |

## 1. 모듈 개요

본 문서는 Conduit(RealWorld) 프로젝트의 핵심 모듈을 LLD 수준으로 정의한다. 각 모듈은 07 HLD §1 참조 -- HLD §1 "핵심 모듈/컴포넌트" 표에서 fan-out된 단위이다.

### 1.1 백엔드 모듈

| 모듈 ID | 모듈명 | 책임 | R-ID 매핑 | F-ID 매핑 | HLD 출처 |
|---|---|---|---|---|---|
| BE-AUTH | Auth Service | 회원가입, 로그인, JWT 발급/검증, 현재 사용자 조회, 사용자 정보 수정 | R-F-01, R-F-02, R-F-03, R-F-04, R-N-03 | F-01, F-02, F-03 | 07 HLD §1 참조 -- Auth 컴포넌트 |
| BE-ARTICLE | Article Service | 아티클 CRUD, slug 생성, 목록/피드 조회, 페이지네이션, 즐겨찾기 관리 | R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-17, R-F-18, R-N-05, R-N-06 | F-05, F-06, F-07, F-09 | 07 HLD §1 참조 -- Article 컴포넌트 |
| BE-COMMENT | Comment Service | 댓글 추가, 목록 조회, 댓글 삭제, 작성자 권한 검증 | R-F-14, R-F-15, R-F-16 | F-08 | 07 HLD §1 참조 -- Comment 컴포넌트 |
| BE-PROFILE | Profile Service | 프로필 조회, 팔로우/언팔로우, following 상태 판정 | R-F-05, R-F-06, R-F-07 | F-03, F-04 | 07 HLD §1 참조 -- Profile 컴포넌트 |
| BE-TAG | Tag Service | 태그 목록 조회, 아티클-태그 연결 관리 | R-F-19 | F-10 | 07 HLD §1 참조 -- Tag 컴포넌트 |

### 1.2 프론트엔드 모듈

| 모듈 ID | 모듈명 | 책임 | R-ID 매핑 | F-ID 매핑 | HLD 출처 |
|---|---|---|---|---|---|
| FE-AUTH | Auth Pages | 회원가입(/register), 로그인(/login), 설정(/settings) 화면 및 인증 상태 관리(JWT localStorage) | R-F-01, R-F-02, R-F-03, R-F-04, R-N-03 | F-01, F-02, F-03 | 07 HLD §1 참조 -- FE Auth 컴포넌트 |
| FE-ARTICLE | Article Pages | 아티클 상세(/article/:slug), 에디터(/editor, /editor/:slug) 화면, 마크다운 렌더링 | R-F-10, R-F-11, R-F-12, R-F-13 | F-07 | 07 HLD §1 참조 -- FE Article 컴포넌트 |
| FE-FEED | Feed/Home Page | 홈(/) 화면, Your Feed/Global Feed/Tag 탭, 아티클 카드 목록, 사이드바 태그, 페이지네이션 | R-F-08, R-F-09, R-F-17, R-F-18, R-F-19, R-N-05 | F-05, F-06, F-09, F-10 | 07 HLD §1 참조 -- FE Feed 컴포넌트 |
| FE-PROFILE | Profile Page | 프로필(/profile/:username, /profile/:username/favorites) 화면, 팔로우/언팔로우 UI, 사용자 아티클/즐겨찾기 탭 | R-F-05, R-F-06, R-F-07 | F-03, F-04 | 07 HLD §1 참조 -- FE Profile 컴포넌트 |

### 1.3 공통 인프라 모듈

| 모듈 ID | 모듈명 | 책임 | R-ID 매핑 | F-ID 매핑 | HLD 출처 |
|---|---|---|---|---|---|
| BE-MIDDLEWARE | Middleware Layer | JWT 인증 미들웨어(필수/선택), 에러 핸들러, CORS, JSON 파싱 | R-N-01, R-N-02, R-N-03, R-N-04 | -- | 07 HLD §1 참조 -- Middleware 컴포넌트 |
| BE-PRISMA | Prisma Data Layer | Prisma Client 싱글턴, 스키마 정의, 마이그레이션, DB 커넥션 관리 | -- | -- | 07 HLD §1 참조 -- Data Layer |
| FE-API | API Client | Axios/fetch 래퍼, base URL 설정, JWT 헤더 자동 부착, 에러 응답 파싱 | R-N-01, R-N-02, R-N-03 | -- | 07 HLD §1 참조 -- FE API Client |
| FE-ROUTER | Router | React Router 해시 라우팅, 인증 가드, 레이아웃(Header/Footer) 래퍼 | -- | -- | 07 HLD §1 참조 -- FE Router |

---

## 2. 외부 인터페이스

### 2.1 BE-AUTH 외부 인터페이스

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

### 3.1 BE-AUTH 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `AuthController` | Router/Controller | Express 라우트 핸들러 -- 요청 파싱, 응답 직렬화 | AuthService, authMiddleware |
| `AuthService` | Service | 비즈니스 로직 -- 가입 검증, 비밀번호 해싱/비교, JWT 생성, 사용자 CRUD | PrismaClient, bcrypt, jsonwebtoken |
| `authMiddleware` | Middleware | JWT 토큰 파싱 및 req.userId 주입 (필수/선택 2가지 모드) | jsonwebtoken |
| `AuthValidator` | Validator | 입력 검증 -- email 형식, username 길이, password 최소 길이 | -- |

### 3.2 BE-ARTICLE 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `ArticleController` | Router/Controller | 라우트 핸들러 -- 목록/피드/단건/CRUD/즐겨찾기 | ArticleService, authMiddleware |
| `ArticleService` | Service | 비즈니스 로직 -- slug 생성, 필터/페이지네이션 쿼리, 즐겨찾기 토글, 작성자 권한 검증 | PrismaClient, slugify |
| `SlugGenerator` | Utility | title에서 URL-safe slug 생성, 중복 시 suffix 추가 | slugify 라이브러리 |
| `ArticleValidator` | Validator | 입력 검증 -- title/description/body 필수 여부, limit/offset 양수 검증 | -- |

### 3.3 BE-COMMENT 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `CommentController` | Router/Controller | 라우트 핸들러 -- 댓글 추가/목록/삭제 | CommentService, authMiddleware |
| `CommentService` | Service | 비즈니스 로직 -- 댓글 생성, 작성자 권한 검증, 아티클 존재 확인 | PrismaClient |
| `CommentValidator` | Validator | 입력 검증 -- body 비어있지 않음, id 양수 정수 | -- |

### 3.4 BE-PROFILE 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `ProfileController` | Router/Controller | 라우트 핸들러 -- 프로필 조회/팔로우/언팔로우 | ProfileService, authMiddleware |
| `ProfileService` | Service | 비즈니스 로직 -- 프로필 조회, Follow 관계 생성/삭제, following 상태 판정 | PrismaClient |

### 3.5 BE-TAG 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `TagController` | Router/Controller | 라우트 핸들러 -- 태그 목록 | TagService |
| `TagService` | Service | 비즈니스 로직 -- 사용 중인 태그 목록 집계 | PrismaClient |

### 3.6 BE-MIDDLEWARE 내부 컴포넌트

| 컴포넌트 | 타입 | 책임 | 의존 |
|---|---|---|---|
| `authRequired` | Middleware | JWT 필수 검증 -- 실패 시 401 | jsonwebtoken |
| `authOptional` | Middleware | JWT 선택 검증 -- 있으면 파싱, 없으면 통과 | jsonwebtoken |
| `errorHandler` | Middleware | 전역 에러 핸들러 -- RealWorld 에러 형식 직렬화 | -- |
| `corsMiddleware` | Middleware | CORS 헤더 설정 (localhost 허용) | cors 패키지 |

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

### 4.1 회원가입 흐름 (R-F-01, F-01)

```
User -> RegisterPage: 폼 입력 (username, email, password)
RegisterPage -> api.post("/api/users"): HTTP POST
Express Router -> AuthController.register: 요청 수신
AuthController -> AuthValidator: 입력 검증
AuthValidator -->> AuthController: 유효/무효
AuthController -> AuthService.register: 비즈니스 로직
AuthService -> bcrypt.hash: 비밀번호 해싱
AuthService -> PrismaClient.user.create: DB 저장
AuthService -> jwt.sign: JWT 생성
AuthService -->> AuthController: User + token
AuthController -->> RegisterPage: 200 { user }
RegisterPage -> useAuth.setUser: JWT localStorage 저장
RegisterPage -> Router.navigate("/"): 홈 리다이렉트
```

### 4.2 로그인 흐름 (R-F-02, F-02)

```
User -> LoginPage: 폼 입력 (email, password)
LoginPage -> api.post("/api/users/login"): HTTP POST
AuthController -> AuthService.login: credentials 검증
AuthService -> PrismaClient.user.findUnique: email로 조회
AuthService -> bcrypt.compare: 비밀번호 비교
AuthService -> jwt.sign: JWT 생성
AuthService -->> AuthController: User + token 또는 에러
AuthController -->> LoginPage: 200 { user } 또는 401/422
LoginPage -> useAuth.setUser: JWT localStorage 저장
LoginPage -> Router.navigate("/"): 홈 리다이렉트
```

### 4.3 아티클 목록/피드 조회 흐름 (R-F-08, R-F-09, F-05, F-06)

```
User -> HomePage: 탭 선택 (Global Feed / Your Feed / Tag)
HomePage -> api.get("/api/articles" 또는 "/api/articles/feed"): 쿼리 파라미터 포함
Express Router -> ArticleController.list 또는 .feed: 요청 수신
ArticleController -> authOptional/authRequired: JWT 파싱
ArticleController -> ArticleService.list/feed: 필터 + 페이지네이션
ArticleService -> PrismaClient.article.findMany: 조건부 조회 (tag/author/favorited/following)
ArticleService -> PrismaClient.article.count: articlesCount
ArticleService -->> ArticleController: { articles[], articlesCount }
ArticleController -->> HomePage: 200 응답
HomePage -> ArticleList: 카드 렌더링
HomePage -> Pagination: 페이지 번호 렌더링
```

### 4.4 아티클 생성 흐름 (R-F-11, F-07)

```
User -> EditorPage: 폼 입력 (title, description, body, tagList)
EditorPage -> api.post("/api/articles"): HTTP POST
ArticleController -> authRequired: JWT 검증
ArticleController -> ArticleValidator: 입력 검증
ArticleController -> ArticleService.create: 비즈니스 로직
ArticleService -> SlugGenerator.generate: title -> slug 변환
ArticleService -> PrismaClient.article.create: 아티클 + 태그 연결 저장 (트랜잭션)
ArticleService -->> ArticleController: Article 객체
ArticleController -->> EditorPage: 200 { article }
EditorPage -> Router.navigate("/article/:slug"): 상세 페이지 이동
```

### 4.5 댓글 추가 흐름 (R-F-14, F-08)

```
User -> CommentSection: 댓글 body 입력, "Post Comment" 클릭
CommentSection -> api.post("/api/articles/:slug/comments"): HTTP POST
CommentController -> authRequired: JWT 검증
CommentController -> CommentValidator: body 비어있지 않음 검증
CommentController -> CommentService.create: 비즈니스 로직
CommentService -> PrismaClient.article.findUnique: slug로 아티클 존재 확인
CommentService -> PrismaClient.comment.create: 댓글 저장
CommentService -->> CommentController: Comment 객체
CommentController -->> CommentSection: 200 { comment }
CommentSection -> 댓글 목록 갱신: 새 댓글 prepend
```

### 4.6 팔로우/언팔로우 흐름 (R-F-06, R-F-07, F-04)

```
User -> ProfileBanner: "Follow" 또는 "Unfollow" 버튼 클릭
ProfileBanner -> api.post/delete("/api/profiles/:username/follow"): HTTP POST/DELETE
ProfileController -> authRequired: JWT 검증
ProfileController -> ProfileService.follow/unfollow: 비즈니스 로직
ProfileService -> PrismaClient.user.findUnique: username 존재 확인
ProfileService -> PrismaClient.follow.create/delete: Follow 관계 생성/삭제
ProfileService -->> ProfileController: Profile (following=true/false)
ProfileController -->> ProfileBanner: 200 { profile }
ProfileBanner -> 버튼 토글: Follow <-> Unfollow
```

### 4.7 즐겨찾기 토글 흐름 (R-F-17, R-F-18, F-09)

```
User -> ArticlePreview 또는 ArticleMeta: 하트 버튼 클릭
Component -> api.post/delete("/api/articles/:slug/favorite"): HTTP POST/DELETE
ArticleController -> authRequired: JWT 검증
ArticleController -> ArticleService.favorite/unfavorite: 비즈니스 로직
ArticleService -> PrismaClient.favorite.create/delete: Favorite 관계 토글
ArticleService -> PrismaClient.favorite.count: favoritesCount 재계산
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

---

## 6. 에러 처리

### 6.1 백엔드 에러 분류

| 에러 | 발생 조건 | 처리 |
|---|---|---|
| 401 Unauthorized | JWT 없음, 만료, 변조 | `{ "errors": { "token": ["is invalid"] } }` 반환. authMiddleware에서 즉시 차단 |
| 403 Forbidden | 타인 아티클 수정/삭제, 타인 댓글 삭제 시도 | `{ "errors": { "article": ["not owned by you"] } }` 반환. Service 레이어에서 소유자 검증 |
| 404 Not Found | 미존재 slug, username, comment id | `{ "errors": { "resource": ["not found"] } }` 반환. Service 레이어에서 조회 실패 시 |
| 422 Unprocessable Entity | 필수 필드 누락, 중복 email/username, body 빈 값 | `{ "errors": { "field": ["error message"] } }` 반환. Validator 또는 Prisma unique constraint |
| 500 Internal Server Error | 예기치 않은 서버 에러, DB 연결 실패 | `{ "errors": { "server": ["internal error"] } }` 반환. errorHandler 미들웨어에서 catch-all |

### 6.2 프론트엔드 에러 분류

| 에러 | 발생 조건 | 처리 |
|---|---|---|
| 네트워크 에러 | BE 서버 미기동, 네트워크 단절 | 사용자에게 "서버에 연결할 수 없습니다" 토스트/메시지 표시 |
| 401 응답 | JWT 만료 또는 변조 | useAuth에서 토큰 삭제 + 로그인 페이지 리다이렉트 |
| 422 응답 | 입력 검증 실패 | 에러 객체에서 필드별 메시지 추출 -> 폼 상단에 에러 리스트 렌더링 |
| 403 응답 | 권한 없는 작업 시도 | "권한이 없습니다" 메시지 표시 (정상 흐름에서는 버튼 미표시로 예방) |
| 404 응답 | 미존재 리소스 접근 | "찾을 수 없습니다" 메시지 또는 홈으로 리다이렉트 |

### 6.3 공통 에러 응답 형식 (R-N-02)

모든 백엔드 에러 응답은 다음 형식을 따른다.

```json
{
  "errors": {
    "field_name": ["error message 1", "error message 2"]
  }
}
```

전역 errorHandler 미들웨어가 throw된 에러를 catch하여 위 형식으로 직렬화한다. Prisma의 PrismaClientKnownRequestError(P2002 unique constraint)도 422 형식으로 변환한다.

---

## 7. 동시성·트랜잭션

### 7.1 트랜잭션 필요 구간

- **아티클 생성 (BE-ARTICLE)**: article 레코드 + articleTag 관계 레코드를 단일 Prisma 트랜잭션(`prisma.$transaction`)으로 처리. 태그가 미존재 시 Tag 레코드 생성 포함.
- **아티클 삭제 (BE-ARTICLE)**: 연관 댓글, 즐겨찾기, 아티클-태그 관계를 cascade 삭제. Prisma schema의 `onDelete: Cascade` 설정으로 DB 수준에서 보장.
- **즐겨찾기 토글 (BE-ARTICLE)**: Favorite 레코드 생성/삭제와 favoritesCount 갱신의 정합성. Prisma의 `_count` 관계 집계로 실시간 count 계산 (별도 counter 컬럼 불필요).

### 7.2 동시성 고려

- **SQLite 단일 writer lock**: SQLite는 write 시 DB 수준 잠금. 로컬 단일 사용자 환경이므로 실질적 동시성 이슈 없음.
- **Slug 중복**: 같은 title로 동시 생성 시 unique constraint 위반 가능. SlugGenerator가 suffix(-1, -2 등)를 추가하되, 최종적으로 DB unique constraint가 안전망 역할.
- **JWT 무상태**: 서버 세션 없음. JWT 검증은 stateless이므로 동시성 이슈 없음.

### 7.3 로컬 실행 환경 특성

본 프로젝트는 로컬 개발 환경(localhost)에서 단일 사용자가 사용하는 것을 전제로 한다. 따라서 높은 동시성 시나리오(race condition, distributed lock 등)는 현 단계에서 설계 범위 밖이다. 추후 다중 사용자 환경으로 확장 시 SQLite에서 PostgreSQL로 전환하고, 트랜잭션 격리 수준(READ COMMITTED 이상) 설정을 검토한다.

---

## 8. 테스트 진입점

### 8.1 백엔드 단위 테스트

| 모듈 | 테스트 대상 | 테스트 파일 (예상) | 검증 항목 |
|---|---|---|---|
| BE-AUTH | AuthService.register | `tests/unit/auth.service.test.ts` | 비밀번호 해싱, JWT 생성, 중복 검증, 입력 유효성 |
| BE-AUTH | AuthService.login | `tests/unit/auth.service.test.ts` | credentials 비교, JWT 생성, 미존재 사용자 에러 |
| BE-AUTH | authMiddleware | `tests/unit/auth.middleware.test.ts` | JWT 파싱 성공/실패, 필수/선택 모드 분기 |
| BE-ARTICLE | ArticleService.create | `tests/unit/article.service.test.ts` | slug 생성, 태그 연결, 필수 필드 검증 |
| BE-ARTICLE | ArticleService.list/feed | `tests/unit/article.service.test.ts` | 필터 조합, 페이지네이션, body 미포함 |
| BE-ARTICLE | SlugGenerator | `tests/unit/slug.test.ts` | 한글/특수문자 처리, 중복 suffix |
| BE-ARTICLE | ArticleService.favorite/unfavorite | `tests/unit/article.service.test.ts` | 토글 정합성, favoritesCount 갱신 |
| BE-COMMENT | CommentService.create/delete | `tests/unit/comment.service.test.ts` | 생성 정상, 작성자 권한 검증, 아티클 존재 확인 |
| BE-PROFILE | ProfileService.follow/unfollow | `tests/unit/profile.service.test.ts` | Follow 관계 생성/삭제, following 상태 판정 |
| BE-TAG | TagService.list | `tests/unit/tag.service.test.ts` | 사용 중인 태그만 반환, 빈 결과 처리 |
| BE-MIDDLEWARE | errorHandler | `tests/unit/error-handler.test.ts` | 422/401/403/404/500 형식 직렬화, Prisma 에러 변환 |

### 8.2 백엔드 통합 테스트

| 모듈 | 테스트 대상 | 테스트 파일 (예상) | 검증 항목 |
|---|---|---|---|
| BE-AUTH | POST /api/users, POST /api/users/login, GET /api/user, PUT /api/user | `tests/integration/auth.test.ts` | HTTP 상태, 응답 형식, 에러 형식, CORS 헤더 |
| BE-ARTICLE | GET/POST/PUT/DELETE /api/articles, favorite/unfavorite | `tests/integration/article.test.ts` | CRUD 전체 흐름, 필터, 페이지네이션, 권한 |
| BE-COMMENT | POST/GET/DELETE /api/articles/:slug/comments | `tests/integration/comment.test.ts` | 댓글 전체 흐름, 권한 검증 |
| BE-PROFILE | GET /api/profiles, follow/unfollow | `tests/integration/profile.test.ts` | 프로필 조회, 팔로우 토글, following 상태 |
| BE-TAG | GET /api/tags | `tests/integration/tag.test.ts` | 태그 목록 반환 형식 |

### 8.3 프론트엔드 단위 테스트

| 모듈 | 테스트 대상 | 테스트 파일 (예상) | 검증 항목 |
|---|---|---|---|
| FE-AUTH | useAuth hook | `src/__tests__/useAuth.test.ts` | 상태 전이(비인증->인증->비인증), localStorage 동기화 |
| FE-AUTH | LoginPage/RegisterPage | `src/__tests__/AuthPages.test.tsx` | 폼 렌더링, 에러 메시지 표시, 리다이렉트 |
| FE-FEED | ArticlePreview | `src/__tests__/ArticlePreview.test.tsx` | 카드 렌더링, 즐겨찾기 버튼 상태 |
| FE-FEED | Pagination | `src/__tests__/Pagination.test.tsx` | 페이지 번호 계산, 클릭 콜백 |
| FE-ARTICLE | CommentSection | `src/__tests__/CommentSection.test.tsx` | 댓글 폼, 댓글 목록 렌더링, 삭제 버튼 조건부 표시 |
| FE-PROFILE | ProfileBanner | `src/__tests__/ProfileBanner.test.tsx` | 팔로우 버튼 토글, 설정 버튼 조건부 표시 |
| FE-API | api client | `src/__tests__/api.test.ts` | JWT 헤더 자동 부착, 에러 응답 파싱, base URL 설정 |

### 8.4 E2E 테스트 (통합 검증)

| 시나리오 | 커버 모듈 | 검증 항목 |
|---|---|---|
| 회원가입 -> 로그인 -> 프로필 수정 | FE-AUTH + BE-AUTH | 인증 전체 흐름, 네비게이션 전환 |
| 아티클 생성 -> 조회 -> 수정 -> 삭제 | FE-ARTICLE + BE-ARTICLE | 아티클 CRUD 전체 흐름, slug 생성/갱신 |
| 글로벌 피드 -> 태그 필터 -> 페이지네이션 | FE-FEED + BE-ARTICLE + BE-TAG | 목록 조회, 필터, 페이지 전환 |
| 팔로우 -> Your Feed 확인 | FE-PROFILE + FE-FEED + BE-PROFILE + BE-ARTICLE | 팔로우 후 피드 반영 |
| 댓글 작성 -> 삭제 | FE-ARTICLE + BE-COMMENT | 댓글 전체 흐름 |
| 즐겨찾기 -> 프로필 Favorites 탭 | FE-FEED + FE-PROFILE + BE-ARTICLE | 즐겨찾기 토글, 카운트, 프로필 탭 연동 |
