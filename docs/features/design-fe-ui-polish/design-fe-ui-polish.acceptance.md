---
doc_type: feature-acceptance
version: v1.0
status: Draft
author: sungjun.choi@board-playground.dev
date: 2026-05-20
gate: feature
related:
  R-ID: []
  F-ID: [F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10]
  supersedes: null
---

# design(frontend): UI 비주얼 production 폴리싱 — Acceptance Criteria

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-20 | Agent (architect) | 초안 — issue #52 mode=design |

## 1. 인수 기준 (Given/When/Then)

### AC-1 토큰 일관성 (BLOCK)
- **Given** `frontend/src/{pages,components}/*.module.css` 8개 파일
- **When** `grep -rEh "#[0-9a-fA-F]{3,6}\b" frontend/src/{pages,components}/*.module.css | wc -l` 실행
- **Then** 결과는 정확히 `0`

### AC-2 모바일 가로 스크롤 부재 (BLOCK)
- **Given** Playwright `viewport: { width: 375, height: 667 }` (iPhone SE 모바일)
- **When** 9개 화면(`/`, `/login`, `/register`, `/settings`, `/editor`, `/article/:slug`, `/profile/:username`, `/profile/:username/favorites`, `/editor/:slug`) 각각 진입
- **Then** `document.documentElement.scrollWidth <= document.documentElement.clientWidth + 1` (가로 스크롤바 없음, ±1px 반올림 허용)

### AC-3 키보드 focus 가시성 (BLOCK)
- **Given** 키보드만 사용 (마우스 없음)
- **When** 9개 화면 각각에서 Tab 키 순차 입력
- **Then** 모든 interactive 요소(button, a, input, textarea, select)에 시각적 focus ring 표시 (CSS `:focus-visible` outline ≥ 2px)

### AC-4 Loading 상태 컴포넌트 (BLOCK)
- **Given** `/`, `/article/:slug`, `/profile/:username` 진입 직후 API 미응답 상태
- **When** 페이지 렌더링
- **Then** `<LoadingState>` 컴포넌트 DOM 노출 (스피너 + label 텍스트). raw `<p>Loading...</p>` 텍스트 노출 ❌

### AC-5 Empty 상태 컴포넌트 (BLOCK)
- **Given** Home 또는 Profile에서 articles 0건 응답
- **When** 페이지 렌더링
- **Then** `<EmptyState>` 컴포넌트 DOM 노출 (icon + title + hint). raw "No articles are here... yet." 텍스트만 노출 ❌

### AC-6 Error 상태 컴포넌트 (BLOCK)
- **Given** Login에서 잘못된 credentials 제출
- **When** 401 응답 수신
- **Then** `<ErrorState errors={...}>` 컴포넌트 DOM 노출 (`role="alert"` 부여). 이전 `<ul className={styles.errorMessages}>` 4중복 ❌

### AC-7 icon-only 버튼 aria-label (BLOCK)
- **Given** Header NavLink에 icon `<i className="ion-compose">`, `<i className="ion-gear-a">` 포함
- **When** 각 NavLink 요소 검사
- **Then** icon `<i>`는 `aria-hidden="true"` + 동반 텍스트 노출 (현재 이미 텍스트 있음 — 추가 보강만)

### AC-8 디자인 토큰 확장 (BLOCK)
- **Given** `frontend/src/styles/global.css`
- **When** `:root` 블록 검사
- **Then** 신규 토큰 ≥ 8개 정의: `--color-bg-inverse`, `--color-text-muted`, `--radius-sm`, `--radius-md`, `--radius-pill`, `--shadow-sm`, `--transition-base`, `--focus-ring`

## 2. Definition of Done (D-06)

### AI 게이트 (D-06 1단, 6축, PR 생성 전)
- [ ] **축1 — 빌드**: `pnpm tsc` 0 error, `pnpm build` 성공
- [ ] **축2 — 단위/통합 테스트**: 본 PR FE-only — 단위 테스트 없음 (presentational). BE 테스트 영향 0
- [ ] **축3 — 린트**: `pnpm lint` warning 0건 (또는 baseline 유지)
- [ ] **축4 — 회귀**: Playwright #24의 13 시나리오 100% 통과 (셀렉터 회귀 0)
- [ ] **축5 — UI 실증 + stylesheet** (ADR-0011 + ADR-0038): gstack `/qa`로 9개 화면 골든패스, `screenshots/*-after.png` 첨부, CSS Modules + global.css 적용 확인
- [ ] **축6 — 3 profile 부팅 검증** (ADR-0037): dev `pnpm dev` ready 신호 / stg·prod는 단일 환경 운영 N/A 명시

### 휴먼 게이트 (D-06 2단, 머지 전)
- [ ] 사람이 로컬에서 dev 서버 부팅 → 9개 화면 클릭 확인 → 회귀 없음
- [ ] 모바일 viewport(Chrome DevTools iPhone SE) 가로 스크롤 0건
- [ ] Tab 키 내비게이션 → focus ring 가시성 확인
- [ ] `tested` 라벨 부착
- [ ] Approve ≥ 1
- [ ] CI green
- [ ] Closes #52 body에 명시

## 3. 비기능 인수

| NFR | 측정 | 임계값 |
| --- | --- | --- |
| 번들 크기 증가 | `pnpm build` dist 크기 비교 | +5% 이하 (state 컴포넌트 3개 추가) |
| 첫 페인트 영향 | Vite dev 서버 콘솔 timing | 회귀 없음 (±10%) |
| a11y violation (axe-core) | Playwright 통합 시 | critical 0건, serious 0건 |
| 시각 회귀 | gstack `/qa` BEFORE vs AFTER 스크린샷 | 의도된 변경만, 깨짐 0건 |

## 4. 회귀 인수

- E2E #24 13 시나리오 100% 통과 (셀렉터 회귀 0)
- 인증/로그인 플로우 정상 (localStorage JWT 유지)
- API 호출 패턴 변경 없음 (FE-only, 백엔드 불변)
- localStorage 키 변경 없음
- React Router 라우트 변경 없음
- Hot reload (Vite HMR) 정상 동작
