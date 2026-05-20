---
doc_type: feature-plan
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

# design(frontend): UI 비주얼 production 폴리싱 — Implementation Plan

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-20 | Agent (architect) | 초안 — issue #52 mode=design |

## 1. 커밋 시퀀스 (DAG)

| # | 커밋 | 영향 파일 | 테스트 추가 | 회귀 위험 |
| --- | --- | --- | --- | --- |
| 0 | `chore(repo): backend formatter + tighten gitignore #52` (이미 완료 — commit 068ea20) | 40 파일 (BE 포맷 + FE lockfile 정리) | N/A | 낮음 (포맷 only) |
| 1 | `design(frontend): expand global design tokens #52` | `global.css` | N/A | 낮음 (additive — 신규 토큰만) |
| 2 | `design(frontend): replace hardcoded colors and spacing with tokens #52` | 8 CSS module | 시각 회귀: Playwright `/qa` BEFORE 스크린샷과 diff | 중간 — 색상 매핑 오류 시 시각 회귀 |
| 3 | `feat(frontend): focus-visible rings and aria-labels for a11y #52` | `header.tsx`, 9 tsx, 8 CSS module | a11y E2E (axe-core 옵션) — Playwright 13 시나리오 회귀 0건 | 낮음 (additive 속성) |
| 4 | `feat(frontend): responsive media queries (768px and 480px) #52` | 4 CSS module (home/article/profile/editor) | Playwright viewport: `iPhone 12`, `iPad mini` | 중간 — flex/grid layout 영향 |
| 5 | `feat(frontend): LoadingState/EmptyState/ErrorState components #52` | `components/state/*.tsx` (신규 3) + 9 tsx 교체 + 4 CSS module 에러 styles 삭제 | 단위: 없음 (presentational). E2E #24 통과 검증 | 중간 — 셀렉터 변경 가능 |
| 6 | `refactor(frontend): consolidate duplicated CSS via /simplify #52` | CSS module 일부 ($composes 또는 global.css 이동) | 시각 회귀 재검증 | 낮음 |
| 7 | `docs(planning): ADR-0042 design token enforcement + CHANGELOG #52` | `docs/planning/adr/0042-*.md`, `CHANGELOG.md`, `10-lld-screen-design.md` | N/A | 없음 |

## 2. 의존성 그래프

```
0 (formatter cleanup, completed)
  └─ 1 (token expand)
       └─ 2 (token replace) ←  깨지면 시각 회귀
            ├─ 3 (a11y)     ←  semantic HTML과 focus 의존
            └─ 4 (responsive)
                  └─ 5 (state UI) ←  컴포넌트 추출 + 적용
                        └─ 6 (simplify)
                              └─ 7 (docs)
```

병렬 가능: 커밋 3 ↔ 4 (a11y와 responsive는 독립). 본 PR에서는 순차 작업해 commit history 가독성 우선.

## 3. 테스트 매핑

| 커밋 | 테스트 추가 위치 | 시나리오 |
| --- | --- | --- |
| 1 | N/A (CSS 변수 추가만 — 시각적 효과 없음) | global.css 토큰 신규 8개 정의 |
| 2 | `frontend/tests/visual-tokens.spec.ts` (신규, 옵션) | grep으로 hex 0건 자동 검증. 시각 스냅샷은 폴리싱 PR이므로 baseline 갱신 허용 |
| 3 | Playwright 13 시나리오 (#24) 전체 재실행 | (1) keyboard tab order — 모든 interactive 요소 focus visible (2) screen reader landmark (`<main>`, `<nav>`) 존재 |
| 4 | Playwright `viewport: { width: 375, height: 667 }` (iPhone SE) | (1) home 사이드바 stacked (2) article meta wrapped (3) profile banner 축소 (4) 가로 스크롤 없음 |
| 5 | Playwright 13 시나리오 전체 — selector 변경 검증 | Loading → spinner element, Empty → 컴포넌트 className 검사, Error → ul role="alert" |
| 6 | 시각 스냅샷 재검증 — 변경 없음 | refactor이므로 시각 동일 |
| 7 | N/A | 문서만 |

## 4. 빌드·실행 검증 단계

```bash
# 1. 빌드 (TypeScript + Vite)
cd frontend
pnpm install
pnpm tsc           # 타입 체크 0건
pnpm build         # production 번들 빌드 성공

# 2. 단위 검증 (자동 — grep 기반 토큰 위반 검출)
! grep -rE "#[0-9a-fA-F]{3,6}\b" src/pages/*.module.css src/components/*.module.css

# 3. dev 서버 + 브라우저 실증 (ADR-0011 강제, AI 게이트 5축)
pnpm dev    # → http://localhost:5173
# gstack /qa 또는 $B로 9개 화면 골든패스 확인 + 스크린샷
# docs/features/design-fe-ui-polish/screenshots/{home,article,profile,…}-after.png

# 4. E2E 회귀 (#24 13 시나리오)
pnpm exec playwright install --with-deps  # 미설치 시
pnpm exec playwright test                 # 13/13 통과

# 5. 3 profile 부팅 검증 (ADR-0037, AI 게이트 6축)
# - dev: pnpm dev (BE 미실행 상태에서 FE만 부팅 — 본 PR FE-only)
# - stg: BE 실행 가정하에 frontend/.env.stg.example 적용
# - prod: pnpm build && pnpm preview
```

## 5. 점진 합의 / 결정 발생 항목

- **D1** (커밋 1): focus-ring 토큰 색상 — `--color-primary` 동일 vs 별도 색상. **결정**: 별도 `--focus-ring: rgba(92, 184, 92, 0.4)` (3px outline)로 분리. 이유: 다크모드 도입 시 focus는 별도 토큰 권장
- **D2** (커밋 2): `#fff` (20건) → 토큰 vs 리터럴 유지. **결정**: 컨텍스트에 따라 — banner 같은 콘텐츠 위 흰색은 신규 `--color-bg-inverse` 토큰. button text 흰색은 토큰 유지
- **D3** (커밋 4): breakpoint — Bootstrap 4의 768px(md)을 그대로 채택. mobile-first vs desktop-first. **결정**: desktop-first (`max-width: 768px`) — 기존 CSS가 desktop 가정으로 작성됨
- **D4** (커밋 5): state 컴포넌트 위치 — `components/state/*` 폴더 분리 vs `components/` 평면. **결정**: 분리. 향후 컴포넌트 라이브러리 추출 시 용이
- **D5** (커밋 5): error 표시 형식 — `<ul>` 유지 vs `<div role="alert">` 변경. **결정**: `<ul role="alert">` — semantic + 스크린리더 즉시 알림
- **D6** (커밋 3): icon-only 버튼 처리 — `<i>` 유지하되 `aria-hidden="true"` + 동반 텍스트로 충분. icon만 보여주는 케이스 없음
