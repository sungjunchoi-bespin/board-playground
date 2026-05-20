---
doc_type: feature-contract
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

# design(frontend): UI 비주얼 production 폴리싱 — Change Contract

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-20 | Agent (architect) | 초안 — issue #52 mode=design |

## 0. 참조 정본 ID (Referenced-IDs)

| 종류 | 정본 위치 | 영향 ID |
| --- | --- | --- |
| F-ID | `docs/planning/05-prd/05-prd.md` | F-01..F-10 (전 화면 영향) |
| R-ID | `docs/planning/04-srs/04-srs.md` | (none — UI polish는 SRS 비기능 요건의 가시성 항목과 매핑되나 신규 R-ID 도입 없음) |
| S-ID | `docs/planning/10-lld-screen-design/10-lld-screen-design.md` | S-01..S-09 (9개 화면 visual 변경) |
| Token | `frontend/src/styles/global.css` §Design Tokens | `--color-*`, `--spacing-*`, `--font-*` (신규: `--color-bg-light`, `--color-text-muted`, `--radius-*`, `--shadow-*`, `--transition-base`, `--focus-ring`) |
| ADR | `docs/planning/adr/0038-frontend-styling-solution.md` (기존), 신규 `0042-design-token-enforcement.md` | ADR-0038 (styling 솔루션), ADR-0042 (본 PR에서 신규) |

## 1. 변경 의도

Sprint 1 종료 후 9개 화면이 기능적으로 동작하지만, **toolkit 정책 ADR-0038(styling 솔루션 강제) + 10-lld-screen-design §3(토큰 정의)** 가 코드 수준에서 실효 강제되지 않은 상태다. CSS module이 토큰을 우회하고 hex/spacing을 직접 작성하고 있어 다크모드·리브랜딩 시 변경 지점이 7개 module 146건으로 분산된다. 또한 a11y·반응형·상태 UI가 production 표준을 충족하지 못한다. 본 PR은 4축에서 동시에 정합화하여 ADR-0011 + ADR-0038 + ADR-0042의 schema-level 강제를 코드 수준에서 실효화한다.

## 2. Before / After

| 항목 | Before | After |
| --- | --- | --- |
| **디자인 토큰** ||
| global.css 토큰 수 | 18개 (color 7, font 6, spacing 5) | 26개 (+ radius 3, shadow 3, transition 1, focus-ring 1, color-text-muted/bg-light 2) |
| CSS module hex 사용 | 146건 (`#5cb85c` 28, `#fff` 20, `#999` 12, …) | 0건 (`grep -E '#[0-9a-f]{3,6}' frontend/src/{pages,components}/*.module.css` → 0) |
| CSS module hardcoded rem/px | spacing 자유 작성 (`0.5rem`, `1rem`, …) | 가능한 모든 spacing은 `var(--spacing-*)` 참조 (border-width `1px` 등 예외 명시) |
| **접근성** ||
| `:focus-visible` 사용 | 0건 (`:focus` 6건만) | 모든 interactive 요소 적용 (`button`, `a`, `input`, `textarea`, `select` ≥ 20곳) |
| icon-only 버튼/링크 aria-label | 0건 (`<i className="ion-compose">` 4건 모두 라벨 없음) | 모두 `aria-label` 부여 + 시각 텍스트 동반 |
| semantic HTML | `<div>` interactive 다수 | `<button>`/`<nav>`/`<main>`/`<article>` 적용 |
| **반응형** ||
| `@media` 쿼리 | 6개 페이지 중 0개 | 4개 페이지(home/article/profile/editor) `@media (max-width: 768px)` + `@media (max-width: 480px)` |
| 768px 가로 스크롤 | Home/Article/Profile/Editor 발생 | 0건 (Playwright viewport test) |
| **상태 UI** ||
| Loading 표현 | `<p>Loading article...</p>` (article), `<p className={styles.loadingMessage}>Loading articles...</p>` (home) — ad-hoc | `<LoadingState label="..." />` 컴포넌트, 9개 화면 사용. 스피너 + 텍스트 |
| Empty 표현 | `<div>No articles are here... yet.</div>` × 2 — 텍스트 only | `<EmptyState icon title hint />` 컴포넌트, 안내 메시지 일관 |
| Error 표현 | `<ul className={styles.errorMessages}>` × 4 (login/register/settings/editor) — 4번 중복 | `<ErrorState errors={...} />` 컴포넌트 1곳 |
| **인터랙션** ||
| 버튼 hover transition | 없음 | `transition: var(--transition-base)` 일괄 |
| 비활성(disabled) 상태 시각 표현 | 없음 | `opacity` 또는 `cursor: not-allowed` |

## 3. 호출자·의존자 (Call Sites)

| 위치 | 영향 | 조치 |
| --- | --- | --- |
| `frontend/src/components/header.tsx` | icon 버튼(`ion-compose`, `ion-gear-a`) 라벨 없음 | `aria-hidden="true"` icon + 텍스트 라벨 유지, NavLink에 `aria-current` 보조 |
| `frontend/src/pages/home-page.tsx` | Loading/Empty 텍스트 → 컴포넌트 | `<LoadingState>`/`<EmptyState>` 치환. 셀렉터 변경 시 Playwright `data-testid` 부여 |
| `frontend/src/pages/article-page.tsx` | Loading 텍스트 → 컴포넌트 | `<LoadingState>` 치환, `<main role="main">` 적용 |
| `frontend/src/pages/profile-page.tsx` | Empty + 탭/배너 반응형 | `<EmptyState>` + `@media` 추가 |
| `frontend/src/pages/login-page.tsx` / `register-page.tsx` / `settings-page.tsx` / `editor-page.tsx` | 에러 표시 `<ul>` 4중복 | `<ErrorState errors={errors} />` 1곳 |
| `frontend/src/components/favorite-button.tsx` | hover transition, focus-visible | CSS module 패치 |
| `frontend/tests/*.spec.ts` (Playwright #24) | 셀렉터 텍스트 매칭 시 잠재적 회귀 | 변경 후 13 시나리오 재실행 — 실패 시 `data-testid` 도입으로 안정화 |
| Backend | 없음 | 변경 없음 |

## 4. Backward Compatibility

- **API 계약**: 변경 없음 (FE-only)
- **DOM 시각 호환성**: 시각적 변경 多 (디자인 의도된 변경). 사용자 시점 깨짐(자리이동 등) 발생 가능 — UI 변경 PR 본질
- **localStorage/세션**: 변경 없음 (`use-auth` 훅 미변경)
- **Playwright E2E #24**: 셀렉터 충돌 가능 — 13 시나리오 재실행 후 회귀 발생 시 PR에서 같이 수정
- **ARIA 라벨 추가**: 신규 속성만 부여, 기존 셀렉터에 영향 없음

## 5. Rollback 전략

- **단위 롤백**: 본 PR은 4축(토큰/a11y/반응형/상태 UI)을 별 commit으로 분리 (commit graph 참조). 특정 축만 revert 가능
- **전체 롤백**: `git revert <merge-commit>` — 백엔드/DB 영향 0건이므로 안전. 디자인 토큰 추가만 잔존 (additive)
- **점진 롤백**: state 컴포넌트만 rollback 시 각 화면 ad-hoc 텍스트로 회귀 — `<LoadingState>` import 삭제 + 인라인 텍스트로 치환
- **데이터 마이그레이션**: 없음

## 6. 비목표

- 자체 호스팅 (Bootstrap/Google Fonts/Ionicons CDN 유지)
- 다크모드
- 컴포넌트 라이브러리 추출
- React Query/SWR
- i18n
- 백엔드 변경
