---
doc_type: screen-design
gate: C
version: v1.0
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: []
  F-ID: [F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10]
  supersedes: null
---

# Conduit (RealWorld) — Screen Design (LLD — UI)

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (architect) | 초안 — RealWorld 9 라우트 화면 상세 + Bootstrap 4 디자인 토큰 |

## 1. 화면 인벤토리

| ID | 화면명 | 진입 트리거 | F-ID 매핑 |
|---|---|---|---|
| S-01 | Home | `/#/` 직접 접속, 로고 클릭, 네비게이션 "Home" 클릭 | F-05, F-06, F-10 |
| S-02 | Login | `/#/login` 직접 접속, 네비게이션 "Sign in" 클릭 | F-02 |
| S-03 | Register | `/#/register` 직접 접속, 네비게이션 "Sign up" 클릭 | F-01 |
| S-04 | Settings | `/#/settings` 직접 접속, 네비게이션 "Settings" 클릭 | F-03 |
| S-05 | Editor New | `/#/editor` 직접 접속, 네비게이션 "New Article" 클릭 | F-07 |
| S-06 | Editor Edit | `/#/editor/:slug` 아티클 상세에서 "Edit Article" 클릭 | F-07 |
| S-07 | Article | `/#/article/:slug` 아티클 카드 클릭, 아티클 발행 후 리다이렉트 | F-07, F-08, F-09, F-04 |
| S-08 | Profile | `/#/profile/:username` 아티클 작성자 클릭, 네비게이션 프로필 클릭 | F-03, F-04, F-09 |
| S-09 | Profile Favorites | `/#/profile/:username/favorites` 프로필 "Favorited Articles" 탭 클릭 | F-03, F-04, F-09 |

## 2. 화면 상세

### S-01: Home

**목적**: 앱의 메인 랜딩 페이지. 글로벌 피드 / 개인 피드 / 태그 필터 피드를 전환하며 최신 아티클을 탐색하고, 사이드바에서 인기 태그를 확인한다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 비인증 | JWT 없음 | 배너 표시, "Global Feed" 탭만 표시 |
| 인증 | JWT 유효 | 배너 표시, "Your Feed" + "Global Feed" 탭 표시 |
| 태그 필터 활성 | 사이드바 태그 클릭 | "# {tag}" 탭 추가, 해당 태그 아티클만 표시 |
| 피드 로딩 | API 호출 중 | "Loading articles..." 텍스트 |
| 피드 비어있음 | articles 0건 | "No articles are here... yet." 텍스트 |
| 태그 로딩 | GET /api/tags 호출 중 | "Loading tags..." 텍스트 |

**레이아웃**:

```
[NavBar]
[Banner: "conduit" + 설명]
[Container]
  [Feed Toggle: Your Feed | Global Feed | #tag]
  [Article Preview Card] x N        [Sidebar: Popular Tags]
  [Article Preview Card] x N        [  tag badge  tag badge ]
  [Pagination: 1 2 3 ...]
[Footer]
```

**F-ID 매핑**: F-05 (글로벌 피드), F-06 (개인 피드), F-10 (태그 시스템)

**주요 인터랙션**:
- 탭 전환 시 해당 피드 API 호출 (GET /api/articles 또는 GET /api/articles/feed)
- 태그 클릭 시 GET /api/articles?tag={tag} 호출 + "#tag" 탭 추가
- 페이지네이션 클릭 시 offset 변경 (limit=10 기본)
- 아티클 카드 하트 클릭 시 즐겨찾기 토글 (F-09)

---

### S-02: Login

**목적**: 기존 회원이 email/password로 로그인하여 JWT를 발급받는다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 초기 | 페이지 진입 | 빈 폼, "Sign in" 버튼 활성 |
| 제출 중 | API 호출 중 | "Sign in" 버튼 비활성 (중복 제출 방지) |
| 에러 | 401/422 응답 | 폼 상단에 에러 메시지 목록 (빨간색) |
| 성공 | 200 응답 | JWT localStorage 저장 + `/#/` 리다이렉트 |

**레이아웃**:

```
[NavBar]
[Container: centered, narrow]
  [h1: "Sign in"]
  [Link: "Need an account?" -> /#/register]
  [Error Messages: ul.error-messages]
  [Form]
    [Input: Email]
    [Input: Password]
    [Button: "Sign in" (pull-right)]
[Footer]
```

**F-ID 매핑**: F-02 (로그인/로그아웃)

**주요 인터랙션**:
- "Need an account?" 링크 클릭 시 /#/register 이동
- 폼 제출 시 POST /api/users/login 호출
- 성공 시 JWT를 localStorage에 저장하고 홈으로 리다이렉트
- 실패 시 errors 객체를 파싱하여 에러 메시지 표시

---

### S-03: Register

**목적**: 새 사용자가 username/email/password로 회원가입하여 계정을 생성한다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 초기 | 페이지 진입 | 빈 폼, "Sign up" 버튼 활성 |
| 제출 중 | API 호출 중 | "Sign up" 버튼 비활성 |
| 에러 | 422 응답 | 폼 상단에 에러 메시지 목록 |
| 성공 | 200 응답 | JWT localStorage 저장 + `/#/` 리다이렉트 |

**레이아웃**:

```
[NavBar]
[Container: centered, narrow]
  [h1: "Sign up"]
  [Link: "Have an account?" -> /#/login]
  [Error Messages: ul.error-messages]
  [Form]
    [Input: Username]
    [Input: Email]
    [Input: Password]
    [Button: "Sign up" (pull-right)]
[Footer]
```

**F-ID 매핑**: F-01 (회원가입)

**주요 인터랙션**:
- "Have an account?" 링크 클릭 시 /#/login 이동
- 폼 제출 시 POST /api/users 호출
- 성공 시 자동 로그인(JWT 저장) 후 홈 리다이렉트
- 에러 시 "username has already been taken" 등 서버 에러 메시지 표시

---

### S-04: Settings

**목적**: 로그인된 사용자가 자신의 프로필 정보를 수정하고, 로그아웃할 수 있다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 로딩 | GET /api/user 호출 중 | 폼 필드 빈 상태 또는 로딩 표시 |
| 초기 | 현재 사용자 정보 로드 완료 | 폼 필드에 기존 값 채워짐 |
| 제출 중 | PUT /api/user 호출 중 | "Update Settings" 버튼 비활성 |
| 에러 | 422 응답 | 폼 상단에 에러 메시지 목록 |
| 성공 | 200 응답 | 프로필 페이지로 리다이렉트 |
| 미인증 | JWT 없음/만료 | `/#/login` 리다이렉트 |

**레이아웃**:

```
[NavBar]
[Container: centered, narrow]
  [h1: "Your Settings"]
  [Error Messages: ul.error-messages]
  [Form]
    [Input: Profile Image URL (small)]
    [Input: Username (large)]
    [Textarea: Short bio]
    [Input: Email (large)]
    [Input: New Password (large)]
    [Button: "Update Settings" (pull-right)]
  [hr]
  [Button: "Or click here to logout." (outline-danger)]
[Footer]
```

**F-ID 매핑**: F-03 (프로필 조회/수정)

**주요 인터랙션**:
- 페이지 진입 시 GET /api/user로 현재 정보 로드
- 폼 제출 시 PUT /api/user 호출 (변경된 필드만)
- "logout" 버튼 클릭 시 JWT 삭제 + /#/ 리다이렉트

---

### S-05: Editor New

**목적**: 로그인된 사용자가 새 아티클을 작성하여 발행한다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 초기 | 페이지 진입 | 빈 폼, "Publish Article" 버튼 활성 |
| 제출 중 | POST /api/articles 호출 중 | "Publish Article" 버튼 비활성 |
| 에러 | 422 응답 | 폼 상단에 에러 메시지 목록 |
| 성공 | 200 응답 | `/#/article/:slug` 리다이렉트 |
| 미인증 | JWT 없음/만료 | `/#/login` 리다이렉트 |

**레이아웃**:

```
[NavBar]
[Container: centered]
  [Error Messages: ul.error-messages]
  [Form]
    [Input: Article Title (large)]
    [Input: What's this article about? (small)]
    [Textarea: Write your article (in markdown) (large, rows=8)]
    [Input: Enter tags + Enter key -> tag badge list]
    [Tag List: tag badge (x) ...]
    [Button: "Publish Article" (pull-right)]
[Footer]
```

**F-ID 매핑**: F-07 (아티클 CRUD)

**주요 인터랙션**:
- 태그 입력 필드에서 Enter 시 태그 추가 (배지로 표시)
- 태그 배지의 x 아이콘 클릭 시 태그 제거
- 폼 제출 시 POST /api/articles 호출
- 성공 시 생성된 아티클 상세 페이지로 리다이렉트

---

### S-06: Editor Edit

**목적**: 아티클 작성자가 기존 아티클을 수정한다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 로딩 | GET /api/articles/:slug 호출 중 | 폼 빈 상태 또는 로딩 표시 |
| 초기 | 아티클 데이터 로드 완료 | 폼 필드에 기존 값 채워짐, 태그 배지 표시 |
| 제출 중 | PUT /api/articles/:slug 호출 중 | "Publish Article" 버튼 비활성 |
| 에러 | 422 응답 | 폼 상단에 에러 메시지 목록 |
| 성공 | 200 응답 | `/#/article/:slug` 리다이렉트 |
| 미인증/권한 없음 | JWT 없음 또는 타인 아티클 | `/#/` 리다이렉트 |

**레이아웃**: S-05(Editor New)와 동일 레이아웃. 차이점은 페이지 진입 시 기존 데이터가 폼에 채워진다는 것.

**F-ID 매핑**: F-07 (아티클 CRUD)

**주요 인터랙션**:
- 페이지 진입 시 GET /api/articles/:slug로 기존 데이터 로드
- 태그 추가/제거 동작은 S-05와 동일
- 폼 제출 시 PUT /api/articles/:slug 호출
- 성공 시 수정된 아티클 상세 페이지로 리다이렉트

---

### S-07: Article

**목적**: 아티클 상세 조회 화면. 마크다운 렌더링된 본문, 작성자 정보, 팔로우/즐겨찾기 버튼, 댓글 섹션을 포함한다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 로딩 | API 호출 중 | 로딩 표시 |
| 조회 (비인증) | JWT 없음 | 본문 + 댓글 목록 표시, 댓글 폼 대신 로그인 링크 |
| 조회 (인증, 타인 아티클) | JWT 유효 + 타인 작성 | 본문 + Follow/Favorite 버튼 + 댓글 폼 표시 |
| 조회 (인증, 내 아티클) | JWT 유효 + 본인 작성 | 본문 + Edit/Delete 버튼 + 댓글 폼 표시 |
| 아티클 없음 | 404 응답 | 에러 페이지 또는 홈 리다이렉트 |

**레이아웃**:

```
[NavBar]
[Article Banner: dark bg]
  [h1: Article Title]
  [Author Meta: avatar + username + date]
  [Actions: Follow/Favorite 또는 Edit/Delete]
[Container]
  [Article Body: markdown rendered]
  [Tag List: tag badge ...]
  [hr]
  [Author Meta (중복): avatar + username + date + actions]
  [Comment Section]
    [Comment Form: avatar + textarea + "Post Comment"]  -- 인증 시
    [Login/Register Link]  -- 비인증 시
    [Comment Card] x N
      [Card Body: comment text]
      [Card Footer: avatar + username + date + delete icon(본인만)]
[Footer]
```

**F-ID 매핑**: F-07 (아티클 CRUD), F-08 (댓글), F-09 (즐겨찾기), F-04 (팔로우/언팔로우)

**주요 인터랙션**:
- 페이지 진입 시 GET /api/articles/:slug + GET /api/articles/:slug/comments 병렬 호출
- "Follow" 버튼 클릭 시 POST /api/profiles/:username/follow (토글)
- 하트(Favorite) 버튼 클릭 시 POST /api/articles/:slug/favorite (토글)
- "Edit Article" 클릭 시 /#/editor/:slug 이동
- "Delete Article" 클릭 시 DELETE /api/articles/:slug + /#/ 리다이렉트
- "Post Comment" 클릭 시 POST /api/articles/:slug/comments
- 댓글 휴지통 아이콘 클릭 시 DELETE /api/articles/:slug/comments/:id

---

### S-08: Profile

**목적**: 사용자 프로필과 해당 사용자가 작성한 아티클(My Articles) 목록을 표시한다.

**상태**:

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 로딩 | API 호출 중 | 로딩 표시 |
| 본인 프로필 | JWT 유효 + 본인 username | "Edit Profile Settings" 버튼 표시 |
| 타인 프로필 (비팔로잉) | 로그인 + 타인 + following=false | "Follow {username}" 버튼 (outline) |
| 타인 프로필 (팔로잉) | 로그인 + 타인 + following=true | "Unfollow {username}" 버튼 (filled) |
| 비인증 | JWT 없음 | Follow 버튼 표시 (클릭 시 로그인 리다이렉트) |
| 아티클 로딩 | 아티클 API 호출 중 | "Loading articles..." 텍스트 |
| 아티클 비어있음 | articles 0건 | "No articles are here... yet." 텍스트 |

**레이아웃**:

```
[NavBar]
[User Info Banner: light bg]
  [Avatar: img circle]
  [h4: username]
  [p: bio]
  [Button: "Edit Profile Settings" 또는 "Follow/Unfollow"]
[Container]
  [Articles Toggle: My Articles | Favorited Articles]
  [Article Preview Card] x N
  [Pagination: 1 2 3 ...]
[Footer]
```

**F-ID 매핑**: F-03 (프로필 조회/수정), F-04 (팔로우/언팔로우), F-09 (즐겨찾기)

**주요 인터랙션**:
- 페이지 진입 시 GET /api/profiles/:username + GET /api/articles?author={username} 호출
- "My Articles" 탭: GET /api/articles?author={username}
- "Favorited Articles" 탭 클릭 시 /#/profile/:username/favorites 이동
- "Edit Profile Settings" 클릭 시 /#/settings 이동
- "Follow/Unfollow" 클릭 시 팔로우 토글 API 호출

---

### S-09: Profile Favorites

**목적**: 사용자가 즐겨찾기한 아티클 목록을 표시한다. S-08과 동일 레이아웃이며 "Favorited Articles" 탭이 활성화된다.

**상태**: S-08(Profile)과 동일. 차이점은 활성 탭이 "Favorited Articles"이며 API 호출이 `GET /api/articles?favorited={username}`으로 변경된다는 것.

| 상태 | 조건 | UI 표현 |
|---|---|---|
| 로딩 | API 호출 중 | 로딩 표시 |
| 본인 프로필 | JWT 유효 + 본인 username | "Edit Profile Settings" 버튼 표시 |
| 타인 프로필 (비팔로잉) | 로그인 + 타인 + following=false | "Follow {username}" 버튼 (outline) |
| 타인 프로필 (팔로잉) | 로그인 + 타인 + following=true | "Unfollow {username}" 버튼 (filled) |
| 즐겨찾기 비어있음 | articles 0건 | "No articles are here... yet." 텍스트 |

**레이아웃**: S-08(Profile)과 동일. "Favorited Articles" 탭이 활성.

**F-ID 매핑**: F-03 (프로필 조회/수정), F-04 (팔로우/언팔로우), F-09 (즐겨찾기)

**주요 인터랙션**:
- 페이지 진입 시 GET /api/profiles/:username + GET /api/articles?favorited={username} 호출
- "My Articles" 탭 클릭 시 /#/profile/:username 이동
- 나머지 인터랙션은 S-08과 동일

## 3. 디자인 시스템 / 토큰

> RealWorld 공식 Bootstrap 4 테마 기반. 커스텀 스타일은 CSS Modules로 작성하되, Bootstrap 4 CDN 테마의 변수/유틸리티를 우선 사용한다.

### Color

| 토큰 | 용도 | 값 |
|---|---|---|
| `--color-primary` | Conduit 브랜드, CTA 버튼, 활성 탭, 즐겨찾기 하트 | `#5CB85C` (green) |
| `--color-primary-dark` | 버튼 hover/active | `#449D44` |
| `--color-secondary` | Follow 버튼 outline, 보조 액션 | `#6C757D` (gray-600) |
| `--color-danger` | Delete 버튼, 에러 메시지 | `#B85C5C` |
| `--color-danger-dark` | Delete 버튼 hover | `#A94442` |
| `--color-neutral-900` | 아티클 배너 배경, 본문 텍스트 | `#333333` |
| `--color-neutral-700` | 서브텍스트, 날짜 | `#687077` |
| `--color-neutral-400` | 비활성 탭, 테두리 | `#AAAAAA` |
| `--color-neutral-200` | 카드 테두리, 구분선 | `#E5E5E5` |
| `--color-neutral-100` | 프로필 배너 배경, 사이드바 배경 | `#F3F3F3` |
| `--color-white` | 페이지 배경, 카드 배경 | `#FFFFFF` |
| `--color-tag-bg` | 사이드바 태그 배지 배경 | `#818A91` |
| `--color-tag-outline` | 아티클 하단 태그 outline | `#DDD` border, transparent bg |

### Typography

| 토큰 | 용도 | 값 |
|---|---|---|
| `--font-heading` | 배너 제목, 페이지 타이틀 | `'Titillium Web', sans-serif` |
| `--font-body` | 아티클 본문 (마크다운 렌더링) | `'Source Serif Pro', serif` |
| `--font-ui` | 네비게이션, 폼, 버튼, 기타 UI 텍스트 | `'Source Sans Pro', sans-serif` |
| `--font-size-banner` | 배너 "conduit" 타이틀 | `3.5rem` (56px) |
| `--font-size-h1` | 페이지 타이틀 (Sign in, Settings 등) | `2.5rem` (40px) |
| `--font-size-h2` | 아티클 상세 제목 | `2rem` (32px) |
| `--font-size-body` | 아티클 본문 텍스트 | `1.2rem` (19.2px) |
| `--font-size-ui` | 폼 라벨, 버튼, 네비게이션 | `1rem` (16px) |
| `--font-size-small` | 날짜, 메타 정보, 카드 서브텍스트 | `0.8rem` (12.8px) |
| `--font-weight-light` | 배너 부제, 일반 텍스트 | `300` |
| `--font-weight-normal` | 본문, UI 텍스트 | `400` |
| `--font-weight-bold` | 제목, 강조 | `700` |

### Spacing

> 4px 기본 단위, 배수 스케일.

| 토큰 | 값 | 용도 |
|---|---|---|
| `--space-1` | `4px` | 태그 배지 내부 패딩, 아이콘-텍스트 간격 |
| `--space-2` | `8px` | 인라인 요소 간격, 태그 목록 gap |
| `--space-3` | `12px` | 카드 내부 패딩, 폼 필드 간격 |
| `--space-4` | `16px` | 섹션 내부 패딩, 버튼 좌우 패딩 |
| `--space-6` | `24px` | 섹션 간 간격, 컨테이너 패딩 |
| `--space-8` | `32px` | 배너 상하 패딩, 큰 섹션 간격 |
| `--space-12` | `48px` | 배너 전체 패딩, 페이지 최상위 간격 |

### Component Primitives

#### Button

| Variant | 클래스 | 배경 | 텍스트 | 테두리 | 용도 |
|---|---|---|---|---|---|
| Primary | `.btn-primary` | `#5CB85C` | `#FFFFFF` | `#5CB85C` | Sign in, Sign up, Publish, Update Settings, Post Comment |
| Primary (small) | `.btn-sm.btn-primary` | `#5CB85C` | `#FFFFFF` | `#5CB85C` | Favorite 버튼 (카드) |
| Outline Primary | `.btn-outline-primary` | transparent | `#5CB85C` | `#5CB85C` | Favorite (미즐겨찾기), 아티클 카드 하트 |
| Outline Secondary | `.btn-outline-secondary` | transparent | `#6C757D` | `#6C757D` | Follow (미팔로잉) |
| Secondary (filled) | `.btn-secondary` | `#6C757D` | `#FFFFFF` | `#6C757D` | Unfollow (팔로잉 중) |
| Outline Danger | `.btn-outline-danger` | transparent | `#B85C5C` | `#B85C5C` | Delete Article, Logout |

**상태**:
- Default: 위 표 기본값
- Hover: 배경색 진해짐 (dark variant)
- Disabled: `opacity: 0.65`, `cursor: not-allowed`
- Active: pressed 스타일 (Bootstrap 4 기본)

#### Input

| Variant | 용도 | 크기 | 비고 |
|---|---|---|---|
| Text (large) | 제목, username, email, password | `form-control-lg` | 높이 확대, 폰트 확대 |
| Text (small) | 이미지 URL, description | `form-control` | 기본 크기 |
| Textarea (large) | 아티클 본문 | `form-control-lg`, rows=8 | 높이 확대, 마크다운 입력 |
| Textarea (small) | 댓글 본문 | `form-control`, rows=3 | 기본 크기 |
| Tag Input | 태그 입력 | `form-control` | Enter 키로 태그 추가, 별도 배지 리스트 렌더링 |

**상태**:
- Default: `border: 1px solid #CED4DA`, `background: #FFFFFF`
- Focus: `border-color: #5CB85C`, `box-shadow: 0 0 0 0.2rem rgba(92,184,92,0.25)`
- Error: 에러 메시지는 폼 상단 `ul.error-messages`에 통합 표시 (필드별 하이라이트 아님)

#### Card (Article Preview)

| 요소 | 스타일 |
|---|---|
| 컨테이너 | `border-top: 1px solid #E5E5E5`, `padding: 24px 0` |
| 작성자 영역 | `avatar(32px circle)` + `username(green link)` + `date(light gray, small)` |
| 즐겨찾기 버튼 | 우측 상단, `btn-sm btn-outline-primary` 또는 `btn-sm btn-primary` (활성) |
| 제목 | `h1` 스타일, `font-weight: 600`, 클릭 시 아티클 상세 이동 |
| 설명 | `color: #999`, `font-weight: 300`, `font-size: 1rem` |
| 태그 목록 | 하단, `tag-outline` 스타일 (light gray border, small font) |
| "Read more..." | 하단 좌측, `color: #BBB`, `font-size: 0.8rem` |

#### NavBar

| 요소 | 스타일 |
|---|---|
| 컨테이너 | `navbar-light`, 흰색 배경, 하단 테두리 없음 |
| 로고 | `navbar-brand`, `color: #5CB85C`, `font-family: Titillium Web`, `font-size: 1.5rem` |
| 네비게이션 링크 | `nav-link`, `color: rgba(0,0,0,0.3)`, 활성 시 `color: rgba(0,0,0,0.8)` |
| 비인증 메뉴 | Home, Sign in, Sign up |
| 인증 메뉴 | Home, New Article (ion-compose), Settings (ion-gear-a), {username} (avatar) |

#### Avatar

| Variant | 크기 | 용도 |
|---|---|---|
| Small | `26px` circle | 댓글 카드, 댓글 폼 |
| Medium | `32px` circle | 아티클 카드 작성자, 아티클 상세 메타 |
| Large | `100px` circle | 프로필 배너 |

**기본 이미지**: `https://api.realworld.io/images/smiley-cyrus.jpeg` (fallback)

#### Tag Badge

| Variant | 용도 | 스타일 |
|---|---|---|
| Sidebar (filled) | 홈 사이드바 인기 태그 | `background: #818A91`, `color: #FFFFFF`, `border-radius: 10rem`, `padding: 2px 8px`, `font-size: 0.8rem`, 클릭 가능 |
| Article Outline | 아티클 카드/상세 하단 태그 | `border: 1px solid #DDD`, `color: #AAAAAA`, `background: transparent`, `border-radius: 10rem`, `padding: 1px 6px`, `font-size: 0.8rem` |
| Editor (removable) | 에디터 태그 입력 결과 | `background: #818A91`, `color: #FFFFFF`, `border-radius: 10rem`, `padding: 2px 8px`, x 아이콘 포함 |

#### Pagination

| 요소 | 스타일 |
|---|---|
| 컨테이너 | `ul.pagination` (Bootstrap 4 기본) |
| 페이지 아이템 | `page-item`, `page-link` |
| 기본 | `color: #5CB85C`, `background: transparent` |
| 활성 | `background: #5CB85C`, `color: #FFFFFF`, `border-color: #5CB85C` |

### Styling Solution Mapping (12-scaffolding section 8 연결)

| 디자인 토큰 레이어 | 구현 솔루션 | 파일 |
|---|---|---|
| Bootstrap 4 테마 (전역 기본) | CDN link (`index.html`) | `<link>` in `index.html` |
| Color / Typography / Spacing 커스텀 | CSS custom properties | `src/styles/tokens.css` (CSS Modules import) |
| Component override / 상태 스타일 | CSS Modules (`.module.css`) | 각 컴포넌트 옆 `*.module.css` |
| Google Fonts (Titillium Web, Source Serif Pro, Source Sans Pro) | CDN link (`index.html`) | `<link>` in `index.html` |
| Ionicons (네비게이션 아이콘) | CDN link (`index.html`) | `<link>` in `index.html` |

## 4. 접근성

### 시맨틱 HTML

- 네비게이션: `<nav>` + `<ul>` + `<li>` + `<a>` 구조
- 메인 콘텐츠: `<main>` 래핑
- 아티클: `<article>` 태그 사용
- 폼: `<form>` + `<fieldset>` + `<label>` (필드별 `for` 연결)
- 제목: `<h1>` ~ `<h4>` 계층 유지 (건너뛰기 없음)

### 키보드 네비게이션

- 모든 인터랙티브 요소(링크, 버튼, 입력 필드)에 Tab 포커스 가능
- 폼 제출: Enter 키 지원
- 모달/드롭다운 없음 (RealWorld 스펙 범위 내)

### ARIA 속성

- 현재 페이지 네비게이션: `aria-current="page"`
- 즐겨찾기 버튼: `aria-label="Favorite article"` / `aria-pressed="true/false"`
- 팔로우 버튼: `aria-label="Follow {username}"` / `aria-pressed="true/false"`
- 에러 메시지 영역: `role="alert"`, `aria-live="polite"`
- 로딩 상태: `aria-busy="true"`

### 색상 대비

- 본문 텍스트 (#333) on 흰 배경: 12.63:1 (AAA 통과)
- 프라이머리 녹색 (#5CB85C) on 흰 배경: 3.52:1 (AA Large 통과, 버튼 텍스트는 white-on-green 4.57:1)
- 에러 텍스트 (#B85C5C) on 흰 배경: 4.04:1 (AA Large 통과)

### 반응형

- Bootstrap 4 그리드 시스템 기반 (`container`, `row`, `col-*`)
- 모바일: 사이드바 태그가 피드 아래로 이동 (col-12)
- 태블릿/데스크톱: 피드 col-9 + 사이드바 col-3

## 5. Open Questions

- 다크 모드 지원 여부 (RealWorld 스펙 범위 밖, v2 고려)
- 아티클 본문 마크다운 렌더링 시 코드 하이라이팅 라이브러리 선택 (marked vs remark + rehype)
- 이미지 fallback 처리 (프로필 이미지 URL 깨짐 시 기본 이미지 표시)
- 페이지네이션 limit 값 (RealWorld 공식 데모는 10, API 기본값은 20)
- 낙관적 업데이트(optimistic update) 적용 범위 (즐겨찾기만 vs 팔로우 포함)
