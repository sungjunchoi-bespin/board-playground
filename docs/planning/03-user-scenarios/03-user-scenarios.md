---
doc_type: user-scenarios
gate: B
version: v1.0
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# Conduit (RealWorld) — 사용자 시나리오

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (analyst) | 초안 — RealWorld 스펙 기반 페르소나·유즈케이스 작성 |

## 1. 페르소나

| 페르소나 | 역할 | 환경 / 컨텍스트 | 주요 목표 |
|---|---|---|---|
| Alice (독자) | 비회원 방문자 | 데스크톱 브라우저, 검색 유입 | 관심 주제 글 탐색, 태그 필터링 |
| Bob (작성자) | 회원 블로거 | 데스크톱/모바일 브라우저 | 기술 글 작성·편집, 독자 반응 확인 |
| Carol (팔로워) | 회원 구독자 | 데스크톱 브라우저 | 관심 작성자 팔로우, 피드 구독, 좋아요 |

## 2. 사용자 여정 (큰 그림)

```
[비회원] → 홈(글로벌 피드) → 태그 클릭 → 아티클 조회
    ↓
  회원가입 → 로그인
    ↓
[회원] → 홈(개인 피드/글로벌 피드) → 아티클 조회 → 댓글 작성 → 즐겨찾기
    ↓                                                    ↓
  프로필 → 팔로우/언팔로우                          아티클 작성/편집 → 발행
    ↓
  설정 → 프로필 수정 → 로그아웃
```

## 3. Use Case

### UC-01: 회원가입

- **Actor**: Alice (비회원)
- **Precondition**: 미로그인 상태
- **Main Flow**:
  1. 홈에서 "Sign up" 클릭
  2. username, email, password 입력
  3. "Sign up" 버튼 클릭
  4. 서버가 사용자 생성 + JWT 발급
  5. 홈 리다이렉트 (로그인 상태)
- **Alternative Flow**:
  - username/email 중복 시 에러 메시지 표시
  - 필수 필드 누락 시 422 에러

### UC-02: 로그인

- **Actor**: Bob (회원)
- **Precondition**: 계정 보유
- **Main Flow**:
  1. "Sign in" 클릭
  2. email, password 입력
  3. "Sign in" 버튼 클릭
  4. JWT 발급, localStorage 저장
  5. 홈 리다이렉트
- **Alternative Flow**:
  - 잘못된 credentials → 에러 메시지
  - 미등록 email → 401 에러

### UC-03: 아티클 작성

- **Actor**: Bob (작성자)
- **Precondition**: 로그인 상태
- **Main Flow**:
  1. "New Article" 클릭 (에디터 페이지)
  2. title, description, body(마크다운), tagList 입력
  3. "Publish Article" 클릭
  4. slug 자동 생성, 아티클 저장
  5. 아티클 상세 페이지 리다이렉트
- **Alternative Flow**:
  - 필수 필드 누락 → 422 에러
  - 미인증 → 401 리다이렉트

### UC-04: 아티클 조회·피드

- **Actor**: Carol (팔로워)
- **Precondition**: 로그인 상태
- **Main Flow**:
  1. 홈에서 "Your Feed" 탭 선택
  2. 팔로잉 사용자의 최신 아티클 목록 (limit 20, offset 0)
  3. 아티클 카드 클릭 → 상세 조회
  4. 마크다운 렌더링된 본문 + 메타데이터 확인
- **Alternative Flow**:
  - 팔로잉 0명 → 빈 피드
  - "Global Feed" 탭 → 전체 아티클 (비로그인도 가능)
  - 태그 클릭 → 해당 태그 필터링

### UC-05: 댓글 작성·삭제

- **Actor**: Carol
- **Precondition**: 로그인 + 아티클 상세 페이지
- **Main Flow**:
  1. 댓글 입력 폼에 body 작성
  2. "Post Comment" 클릭
  3. 댓글 목록에 실시간 반영
- **Alternative Flow**:
  - 자기 댓글 삭제 버튼 표시 → 클릭 → 삭제
  - 타인 댓글 → 삭제 버튼 미표시
  - 미인증 → 댓글 폼 미표시

### UC-06: 즐겨찾기 (Favorite)

- **Actor**: Carol
- **Precondition**: 로그인 상태
- **Main Flow**:
  1. 아티클 카드/상세에서 하트 버튼 클릭
  2. `POST /api/articles/:slug/favorite`
  3. favoritesCount 증가, favorited=true
- **Alternative Flow**:
  - 이미 즐겨찾기 → 하트 클릭 시 해제 (DELETE)
  - 미인증 → 401

### UC-07: 팔로우·언팔로우

- **Actor**: Carol
- **Precondition**: 로그인 + 타인 프로필 페이지
- **Main Flow**:
  1. "Follow" 버튼 클릭
  2. `POST /api/profiles/:username/follow`
  3. 버튼이 "Unfollow"로 변경
- **Alternative Flow**:
  - 이미 팔로잉 → "Unfollow" 클릭 시 해제
  - 자기 프로필 → Follow 버튼 미표시, "Edit Profile Settings" 표시

### UC-08: 프로필 수정

- **Actor**: Bob
- **Precondition**: 로그인 상태
- **Main Flow**:
  1. "Settings" 클릭
  2. image URL, username, bio, email, password 수정
  3. "Update Settings" 클릭
  4. `PUT /api/user` 호출
- **Alternative Flow**:
  - "Logout" 클릭 → JWT 삭제, 홈 리다이렉트
  - email 중복 → 422 에러

### UC-09: 아티클 편집·삭제

- **Actor**: Bob (아티클 작성자)
- **Precondition**: 로그인 + 자기 아티클
- **Main Flow (편집)**:
  1. 아티클 상세에서 "Edit Article" 클릭
  2. 에디터에 기존 내용 로드
  3. 수정 후 "Publish Article" 클릭
- **Main Flow (삭제)**:
  1. 아티클 상세에서 "Delete Article" 클릭
  2. `DELETE /api/articles/:slug`
  3. 홈 리다이렉트
- **Alternative Flow**:
  - 타인 아티클 → Edit/Delete 버튼 미표시

## 4. 비기능 시나리오

- **응답 시간**: API 응답 200ms 이내 (p95)
- **동시 접속**: 100 concurrent users 지원
- **보안**: JWT 토큰 만료 관리, 비밀번호 해싱 (bcrypt)
- **접근성**: HTML 시맨틱 태그 사용, Bootstrap 4 반응형

## 5. Open Questions

- 아티클 body에 XSS 방지 처리 범위 (마크다운 렌더링 시 sanitize)
- 페이지네이션 UI (숫자 페이지 vs 무한 스크롤) — RealWorld 스펙은 숫자 페이지
