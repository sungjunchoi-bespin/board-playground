---
doc_type: feature-risk
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

# design(frontend): UI 비주얼 production 폴리싱 — Feature Risk

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-20 | Agent (architect) | 초안 — issue #52 mode=design |

## 1. 본 변경의 리스크

| RISK-ID | 제목 | 영향(1~5) | 가능성(1~5) | 등급 |
| --- | --- | --- | --- | --- |
| R-DP-01 | Playwright E2E #24 셀렉터 회귀 — 텍스트 매칭 시 Loading/Empty 컴포넌트 교체로 깨짐 | 3 | 3 | **Medium** |
| R-DP-02 | 색상 토큰 치환 매핑 오류 — `#5cb85c`(28건) 외 유사 hex(`#449d44`, `#3d8b3d`)를 잘못 매핑하면 시각 회귀 | 3 | 2 | Medium-Low |
| R-DP-03 | 반응형 미디어쿼리 도입으로 기존 desktop 레이아웃에 의도치 않은 영향 (flex/grid wrapping) | 2 | 2 | Low |
| R-DP-04 | aria-* 속성 + semantic HTML 변경으로 CSS 셀렉터(`.nav-item > .nav-link`) 깨짐 | 2 | 1 | Low |
| R-DP-05 | state 컴포넌트 추출 — props 시그니처 잘못 잡으면 9개 화면 일괄 재작업 | 3 | 1 | Low |
| R-DP-06 | Bootstrap 4 CSS와 신규 CSS Module이 충돌 (specificity 경쟁) | 2 | 2 | Low |
| R-DP-07 | 번들 크기 증가 — state 컴포넌트 3개 추가로 번들 +∆ | 1 | 2 | Negligible |

**총평**: 본 PR은 FE-only, 백엔드/DB/API 변경 0. 최대 등급 **Medium**. ADR-0019의 High 등급(롤아웃 게이트 필요) 미해당.

## 2. 리스크 상세

### R-DP-01 — Playwright 셀렉터 회귀 (Medium)
- **시나리오**: #24의 `homepage.spec.ts`가 `page.getByText('Loading articles...')` 같은 텍스트 셀렉터 사용 시 `<LoadingState>` 컴포넌트 교체로 텍스트가 컴포넌트 내부로 이동
- **완화**:
  1. 커밋 5(state 컴포넌트) 전 Playwright 13 시나리오 전체 그린 확인
  2. 컴포넌트 내부에 `data-testid` 부여 (`<LoadingState data-testid="loading-articles">`)
  3. Playwright 셀렉터를 `getByTestId` 우선 사용으로 점진 마이그레이션
- **대응**: 회귀 발생 시 동일 PR에서 셀렉터 수정 (별 hotfix PR 금지 — ADR-0040)

### R-DP-02 — 색상 매핑 오류 (Medium-Low)
- **시나리오**: `#5cb85c`(primary) → `var(--color-primary)`, `#449d44`(primary-dark) → `var(--color-primary-dark)` 매핑을 잘못해 모든 hover 색상이 primary로 통합되는 경우
- **완화**:
  1. 매핑 표 작성 (commit 2 message에 포함)
  2. sed 일괄 치환 금지 — 컨텍스트 보고 수동 치환
  3. before/after 스크린샷 6쌍 비교 (Home, Article, Profile, Editor, Settings, Auth)
- **대응**: 시각 회귀 발견 시 토큰 정의 수정으로 처리 (CSS module은 정상)

### R-DP-03 — 반응형 부수 효과 (Low)
- **시나리오**: 신규 `@media (max-width: 768px)` 추가가 desktop 768px 경계에서 의도치 않게 발동
- **완화**:
  1. Vite dev 서버에서 viewport 700px / 768px / 769px / 800px 4단계 시각 검증
  2. flex/grid 변경은 `flex-direction: column`만 적용, gap·padding 최소 수정

### R-DP-04 — semantic HTML 셀렉터 (Low)
- **시나리오**: `<div className="navbar">` → `<nav className="navbar">` 변경 시 CSS 셀렉터 `div.navbar`가 깨짐
- **완화**: 본 PR에서 className 변경 없이 tag만 교체. CSS는 className 기반이므로 영향 없음

### R-DP-05 — state 컴포넌트 props 설계 (Low)
- **완화**: props 시그니처 미리 정의 →
  ```ts
  type LoadingStateProps = { label?: string; size?: 'sm' | 'md' | 'lg' }
  type EmptyStateProps = { icon?: string; title: string; hint?: string; action?: ReactNode }
  type ErrorStateProps = { errors: string[]; title?: string }
  ```
- **대응**: 1차 적용 후 부족하면 props 추가 (호환성 유지)

### R-DP-06 — Bootstrap specificity (Low)
- **완화**: CSS Module 우선 적용. Bootstrap class 사용 시 :where() 또는 module class 후행
- **대응**: 충돌 발견 시 module class에 `!important` 대신 셀렉터 강화

### R-DP-07 — 번들 크기 (Negligible)
- **현재 dist 크기**: 알려진 baseline 없음. 본 PR에서 baseline 기록 후 +5% 임계값 설정
- **state 컴포넌트 추가**: 3개 × 평균 30 LOC = ~3KB. 영향 미미

## 3. High 등급 단계적 롤아웃

해당 없음 (최고 등급 Medium).

본 PR은 단일 PR로 일괄 머지. 단계적 롤아웃 불필요. 단, 다음 안전장치 적용:

- 커밋 단위 분리 (커밋 1~7) — 특정 축만 revert 가능
- before/after 스크린샷 첨부 — 시각 회귀 즉시 감지
- E2E 회귀 검증 — PR 생성 전 13/13 통과

## 4. 데이터 영속성 변경

없음. 본 PR은 FE-only, DB·localStorage·세션·API 응답 캐시 변경 0.

## 5. 15-risk.md 갱신 항목

- 신규 등재 없음 (Medium 등급 — 본 PR 내 처리)
- 기존 리스크 회귀:
  - 15-risk.md R-FE-01 "디자인 시스템 부재로 인한 폴리싱 비용"이 본 PR로 부분 해소 → 상태 갱신 (Open → Mitigated)
- 잔여 리스크:
  - CDN 의존(Bootstrap/Fonts/Ionicons) — 비목표. 별도 이슈로 이관
