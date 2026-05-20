---
doc_type: feature-brief
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

# design(frontend): UI 비주얼 production 폴리싱 — Feature Brief

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-20 | Agent (architect) | 초안 — issue #52 mode=design |

## 1. 한 줄 의도

Sprint 1에서 빠르게 구현한 9개 화면을 **디자인 토큰 일관성·접근성·반응형·상태 UI 일관화**의 4축에서 production 품질로 끌어올린다.

## 2. 사용자 가치

- **모바일 사용자**: 768px 이하 뷰포트에서 가로 스크롤 없이 모든 화면 사용 가능 (현재 home 사이드바·article meta·profile 배너 깨짐)
- **키보드/스크린리더 사용자**: focus-visible 링과 aria-label 덕에 키보드만으로 9개 화면 모두 조작 가능 (현재 focus 스타일 0건)
- **모든 사용자**: 로딩·빈상태·에러를 일관된 컴포넌트로 표현해 인지 부하 감소 (현재 화면마다 다른 형식)
- **운영자/디자이너**: 색상·여백 변경 시 `global.css` 1곳만 수정 (현재 7개 module 146개 hex 분산)

## 3. 현재 상태 → 변경 후 상태

| 측면 | 현재 | 변경 후 |
| --- | --- | --- |
| 디자인 토큰 사용률 | 7개 CSS module 중 0% (146개 hex 하드코딩) | 0건 (검증: `grep -E '#[0-9a-f]{3,6}' frontend/src/{pages,components}/*.module.css` → 0) |
| `:focus-visible` 스타일 | 6개 (모두 `:focus`, focus-visible 없음) | ≥ 20개, 모든 interactive 요소 적용 |
| `aria-*` 속성 | 0개 (icon-only `<i>` 4건 라벨 없음) | icon 버튼·링크에 aria-label, ul role 등 ≥ 15개 |
| `@media` 쿼리 | 6개 페이지 중 0개 | 4개 페이지(home/article/profile/editor) 768px·480px 적용 |
| 상태 UI 패턴 | 각 화면 ad-hoc 텍스트(`<p>Loading...</p>` 등) | `LoadingState`/`EmptyState`/`ErrorState` 3개 컴포넌트, 9개 화면 사용 |
| 인터랙션 polish | hover transition 없음 | 버튼·태그·링크 `transition: all 0.15s ease` |

## 4. 모드 자동 감지 결과

- **결정**: mode=design
- **시그널**: 사용자 자연어 "디자인", "production 수준", "UI 검토" → ADR-0032 §2.1 단일 시그널, 부정 시그널 0건 → 자동 진행 (질문 없이)
- **수동 override**: 없음
- **PR 본문 Mode Decision Trace 후보**: `mode=design (auto): keyword "디자인" + "production 수준" matched, no conflicting signal`

## 5. 영향 범위

### 직접 변경 파일 (≈12개)
- `frontend/src/styles/global.css` — 디자인 토큰 보강 (focus-ring, border-radius, transition, shadow 토큰 추가)
- `frontend/src/pages/*.module.css` × 7 — hex/spacing 하드코딩 → `var(--*)` 치환, 미디어쿼리 추가
- `frontend/src/components/favorite-button.module.css` — 동일
- `frontend/src/components/header.tsx` — icon 버튼 aria-label, semantic 보강
- `frontend/src/components/state/loading-state.tsx` — 신규
- `frontend/src/components/state/empty-state.tsx` — 신규
- `frontend/src/components/state/error-state.tsx` — 신규
- `frontend/src/pages/*.tsx` × 9 — 로딩/빈상태/에러 컴포넌트 적용

### 간접 영향
- Playwright E2E 13 시나리오(#24) — 셀렉터·텍스트 변경 시 회귀. data-testid를 유지하면 영향 없을 것으로 예상

### 영향 없음
- 백엔드 — 변경 없음 (#52는 frontend-only)
- API 계약 — 변경 없음
- DB 스키마 — 변경 없음
- 빌드 도구 — Vite·TypeScript 설정 동일

## 6. 비목표

- ❌ Bootstrap 4 CDN → 자체 호스팅 전환 — production hardening의 별도 이슈
- ❌ Google Fonts·Ionicons CDN → self-host — 별도 이슈
- ❌ 다크모드 토글 — 토큰 일관화 후 후속 작업
- ❌ 컴포넌트 라이브러리(Button/Card/Tag) 추출 — 폴리싱 후 별도 평가
- ❌ React Query/SWR 도입 — 상태 관리 리팩토링은 별개
- ❌ i18n — 영어 텍스트 유지

## 7. Open Questions

- (해소됨) "디자인"의 범위 — 사용자 확인 결과 프론트엔드 UI 비주얼 (`AskUserQuestion` 응답)
- (해소됨) 워킹트리 변경 처리 — 사용자 확인 결과 새 이슈에 동봉 (commit 068ea20)
- 잔여: 없음
