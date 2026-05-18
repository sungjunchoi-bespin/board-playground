---
doc_type: prd
gate: B
version: v1.1
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: [R-F-01, R-F-02, R-F-03, R-F-04, R-F-05, R-F-06, R-F-07, R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-14, R-F-15, R-F-16, R-F-17, R-F-18, R-F-19]
  F-ID: [F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10]
  supersedes: null
---

# Conduit (RealWorld) — PRD

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.1 | 2026-05-18 | Agent (analyst) | 로컬 전용 실행 방침 반영 — 제품 개요·의존성·Open Questions 조정 |
| v1.0 | 2026-05-18 | Agent (analyst) | 초안 — RealWorld 스펙 기반 기능 정의·MVP Cut |

## 1. 제품 개요

Conduit는 Medium.com 클론 블로그 플랫폼으로, RealWorld 프로젝트 공식 스펙을 100% 준수하는 풀스택 애플리케이션이다. 사용자가 아티클을 작성·공유하고, 다른 사용자를 팔로우하며, 댓글과 즐겨찾기로 상호작용하는 소셜 블로그 서비스를 제공한다. 현 단계에서는 로컬 개발 환경(localhost)에서만 실행하며, 추후 클라우드 배포로 확장할 수 있는 구조를 갖춘다.

## 2. 사용자 가치

- **독자**: 태그·피드 기반으로 관심 분야 글을 효율적으로 탐색
- **작성자**: 마크다운으로 기술 글을 작성·관리, 독자 반응(즐겨찾기·댓글) 확인
- **커뮤니티**: 팔로우·피드 기반 사용자 간 연결, 태그 생태계로 콘텐츠 분류

## 3. 기능

### F-01: 회원가입

- **MVP**: ✅ 포함
- **우선순위**: P0
- **R-ID 매핑**: R-F-01
- **사용자 스토리**: As a 방문자, I want to 회원가입을 하여 So that 아티클 작성·댓글·즐겨찾기 등 인증 기능을 사용할 수 있다.
- **Acceptance**: Given 회원가입 페이지, When username/email/password 입력 후 "Sign up" 클릭, Then 계정 생성 + JWT 발급 + 홈 리다이렉트
- **테스트 시나리오**:
  - 정상: 유효 데이터 → 가입 성공 + 로그인 상태 (성공)
  - 실패: 중복 email/username → 에러 메시지 표시 (에러)
  - 실패: 필수 필드 빈 값 → 폼 검증 에러 (예외)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-02: 로그인·로그아웃

- **MVP**: ✅ 포함
- **우선순위**: P0
- **R-ID 매핑**: R-F-02, R-F-03
- **사용자 스토리**: As a 회원, I want to email/password로 로그인·로그아웃 So that 인증 상태를 관리한다.
- **Acceptance**: Given 로그인 페이지, When 올바른 credentials 입력 후 "Sign in" 클릭, Then JWT localStorage 저장 + 헤더 인증 상태 변경
- **테스트 시나리오**:
  - 정상: 올바른 credentials → 로그인 성공 + 네비게이션 변경 (성공)
  - 정상: 로그아웃 → JWT 삭제 + 비인증 네비게이션 복원
  - 실패: 잘못된 password → 에러 메시지 (에러)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-03: 프로필 조회·수정

- **MVP**: ✅ 포함
- **우선순위**: P1
- **R-ID 매핑**: R-F-04, R-F-05
- **사용자 스토리**: As a 회원, I want to 내 프로필(이미지·이름·소개·이메일·비밀번호)을 수정 So that 다른 사용자에게 보이는 정보를 관리한다.
- **Acceptance**: Given 설정 페이지, When 필드 수정 후 "Update Settings" 클릭, Then 프로필 갱신
- **테스트 시나리오**:
  - 정상: bio 수정 → 프로필에 반영 (성공)
  - 실패: 중복 email → 422 에러 표시 (에러)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-04: 팔로우·언팔로우

- **MVP**: ✅ 포함
- **우선순위**: P1
- **R-ID 매핑**: R-F-06, R-F-07
- **사용자 스토리**: As a 회원, I want to 다른 사용자를 팔로우/언팔로우 So that 개인 피드에서 해당 사용자의 글을 볼 수 있다.
- **Acceptance**: Given 타인 프로필 페이지, When "Follow" 버튼 클릭, Then following=true + 버튼 "Unfollow"로 변경
- **테스트 시나리오**:
  - 정상: 팔로우 → 버튼 토글 + 피드 반영 (성공)
  - 정상: 언팔로우 → 버튼 원복
  - 실패: 미인증 → 401 (에러)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-05: 글로벌 피드

- **MVP**: ✅ 포함
- **우선순위**: P0
- **R-ID 매핑**: R-F-08
- **사용자 스토리**: As a 방문자/회원, I want to 최신 아티클 목록을 페이지네이션으로 탐색 So that 관심 글을 발견한다.
- **Acceptance**: Given 홈페이지 "Global Feed" 탭, When 페이지 로드, Then 최신순 아티클 카드 목록 + 페이지네이션
- **테스트 시나리오**:
  - 정상: 아티클 존재 → 최신순 카드 목록 (성공)
  - 정상: 태그 클릭 → 해당 태그 필터
  - 실패: 아티클 0건 → 빈 상태 UI (예외)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-06: 개인 피드

- **MVP**: ✅ 포함
- **우선순위**: P0
- **R-ID 매핑**: R-F-09
- **사용자 스토리**: As a 회원, I want to 팔로잉 사용자의 아티클만 보는 피드 So that 관심 작성자의 새 글을 쉽게 확인한다.
- **Acceptance**: Given 로그인 + 홈 "Your Feed" 탭, When 탭 선택, Then 팔로잉 사용자 아티클만 표시
- **테스트 시나리오**:
  - 정상: 팔로잉 사용자 글 → 피드에 노출 (성공)
  - 정상: 팔로잉 0명 → 빈 피드
  - 실패: 미인증 → "Your Feed" 미표시 또는 401 (에러)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-07: 아티클 CRUD

- **MVP**: ✅ 포함
- **우선순위**: P0
- **R-ID 매핑**: R-F-10, R-F-11, R-F-12, R-F-13
- **사용자 스토리**: As a 작성자, I want to 아티클을 작성·조회·편집·삭제 So that 내 글을 관리한다.
- **Acceptance**: Given 에디터 페이지, When title/description/body/tags 입력 후 "Publish Article" 클릭, Then 아티클 생성 + 상세 페이지 이동
- **테스트 시나리오**:
  - 정상: 전체 필드 입력 → 생성 + 마크다운 렌더링 확인 (성공)
  - 정상: 편집 → 기존 데이터 로드 + 수정 반영
  - 정상: 삭제 → 홈 리다이렉트
  - 실패: 필수 필드 누락 → 422 에러 (에러)
  - 실패: 타인 아티클 편집/삭제 시도 → 버튼 미표시 (거부)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-08: 댓글

- **MVP**: ✅ 포함
- **우선순위**: P1
- **R-ID 매핑**: R-F-14, R-F-15, R-F-16
- **사용자 스토리**: As a 회원, I want to 아티클에 댓글을 작성·조회·삭제 So that 글에 대한 의견을 나눈다.
- **Acceptance**: Given 아티클 상세 페이지, When 댓글 body 입력 후 "Post Comment" 클릭, Then 댓글 목록에 추가
- **테스트 시나리오**:
  - 정상: 댓글 작성 → 목록에 즉시 반영 (성공)
  - 정상: 자기 댓글 삭제 → 목록에서 제거
  - 실패: 미인증 → 댓글 폼 미표시 (에러)
  - 실패: 타인 댓글 → 삭제 버튼 미표시 (거부)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-09: 즐겨찾기

- **MVP**: ✅ 포함
- **우선순위**: P1
- **R-ID 매핑**: R-F-17, R-F-18
- **사용자 스토리**: As a 회원, I want to 아티클을 즐겨찾기/해제 So that 마음에 드는 글을 나중에 다시 찾는다.
- **Acceptance**: Given 아티클 카드/상세, When 하트 버튼 클릭, Then favorited 토글 + favoritesCount 갱신
- **테스트 시나리오**:
  - 정상: 즐겨찾기 → 카운트 증가 + 하트 활성 (성공)
  - 정상: 해제 → 카운트 감소
  - 실패: 미인증 → 401 (에러)
- 단위: ✅ | 통합: ✅ | E2E: ✅

### F-10: 태그 시스템

- **MVP**: ✅ 포함
- **우선순위**: P1
- **R-ID 매핑**: R-F-19, R-F-08
- **사용자 스토리**: As a 방문자/회원, I want to 인기 태그 목록을 보고 클릭해 필터링 So that 관심 분야 글을 빠르게 탐색한다.
- **Acceptance**: Given 홈 사이드바, When 태그 클릭, Then 해당 태그 아티클만 필터링
- **테스트 시나리오**:
  - 정상: 태그 목록 표시 + 클릭 필터 (성공)
  - 실패: 태그 0건 → 빈 사이드바 (예외)
- 단위: ✅ | 통합: ✅ | E2E: ✅

## 4. MVP Cut 요약

| F-ID | MVP | 비고 |
|---|---|---|
| F-01 | ✅ | 회원가입 — 핵심 인증 |
| F-02 | ✅ | 로그인·로그아웃 — 핵심 인증 |
| F-03 | ✅ | 프로필 조회·수정 — 사용자 관리 |
| F-04 | ✅ | 팔로우·언팔로우 — 소셜 핵심 |
| F-05 | ✅ | 글로벌 피드 — 핵심 탐색 |
| F-06 | ✅ | 개인 피드 — 핵심 구독 |
| F-07 | ✅ | 아티클 CRUD — 핵심 콘텐츠 |
| F-08 | ✅ | 댓글 — 상호작용 |
| F-09 | ✅ | 즐겨찾기 — 상호작용 |
| F-10 | ✅ | 태그 시스템 — 콘텐츠 분류 |

> RealWorld 스펙이 정의한 전체 기능이 MVP 범위. 스펙 외 확장(실시간 알림, 소셜 로그인, 검색 등)은 v2 이후.

## 5. UX 원칙 / 화면 구성 큰 그림

### UX 원칙

- **RealWorld 공식 Bootstrap 4 테마 준수**: 커스텀 디자인 없음
- **SPA 해시 라우팅**: `/#/` prefix (프레임워크별 히스토리 모드 선택 가능)
- **인증 상태 반영 네비게이션**: 비인증(Home/Sign in/Sign up) vs 인증(Home/New Article/Settings/Profile)
- **즉각 반응**: 즐겨찾기·팔로우 버튼 클릭 즉시 UI 반영 (optimistic update)

### 화면 구성

```
[Header] — 네비게이션 (인증 상태별 변경)
   ├── Home (#/) — 배너 + 피드탭(Your/Global/Tag) + 아티클 카드 + 사이드바(태그) + 페이지네이션
   ├── Sign in (#/login) — 로그인 폼
   ├── Sign up (#/register) — 회원가입 폼
   ├── Settings (#/settings) — 프로필 수정 폼 + 로그아웃
   ├── Editor (#/editor, #/editor/:slug) — 아티클 작성/편집 폼
   ├── Article (#/article/:slug) — 배너 + 본문(마크다운) + 댓글
   └── Profile (#/profile/:username, #/profile/:username/favorites) — 프로필 + 아티클 탭
[Footer] — 로고 + 크레딧
```

## 6. 의존성 / 외부 시스템

| 항목 | 설명 |
|---|---|
| Bootstrap 4 테마 | 로컬 번들 우선 (CDN fallback: `https://demo.productionready.io/main.css`) |
| Ionicons | 아이콘 CDN (로컬 환경 인터넷 연결 가정) |
| Google Fonts | Titillium Web, Source Serif Pro 등 (CDN) |
| 마크다운 렌더러 | 아티클 본문 렌더링 (라이브러리 선택은 Gate C) |

> 외부 API 의존 없음 — 인증·데이터 모두 자체 백엔드. 클라우드 서비스 의존 없음 (로컬 전용).

## 7. Open Questions

- 프론트엔드 프레임워크 선택 (React / Vue / Svelte 등) — Gate C
- 백엔드 프레임워크 선택 (Express / Spring Boot / Django 등) — Gate C
- DB: SQLite 우선 (로컬 전용 — 설치 부담 최소), ORM 추상화로 추후 PostgreSQL 전환 경로 — Gate C
- 마크다운 라이브러리 선택 (marked / remark 등) — Gate C
- 배포 전략은 현 단계 제외 — 추후 확장 시 결정
