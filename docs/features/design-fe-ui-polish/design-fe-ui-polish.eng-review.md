---
doc_type: feature-eng-review
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

# design(frontend): UI 비주얼 production 폴리싱 — Engineering Review

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-20 | Agent (architect) | 초안 — issue #52 mode=design |

## 0. Verdict

- reviewer: [reviewer] sungjun.choi@board-playground.dev
- review_at: [review_at] 2026-05-20
- decision: **PASS** — 본 plan은 구현 진입 가능.

근거 요약:
- Contract Before/After 정량 데이터(146건 hex, 0건 focus-visible 등) 기반 — 가설 아님
- 단계적 커밋 시퀀스(8단계) — 단위 revert 가능
- E2E 회귀 위험을 명시적으로 Medium 등급 인식 + 완화책 보유
- 백엔드/DB/API 영향 0건 — blast radius 작음
- ADR-0011 + ADR-0038 + ADR-0042의 schema-level 강제를 코드 수준에서 실효화 (정책 따라잡기)

## 1. Contract 검토

| 항목 | 평가 | 코멘트 |
|---|---|---|
| §0 Referenced-IDs | ✅ | F-01~F-10, S-01~S-09, ADR-0038/0042 명시 |
| §2 Before/After 정량 | ✅ | 146건 → 0건 등 측정 가능 |
| §3 Call Sites | ✅ | 12개 직접 + Playwright 1개 간접 |
| §4 Backward Compat | ✅ | FE-only, API/localStorage 불변 |
| §5 Rollback | ✅ | 단위 + 전체 + 점진 3단 |

## 2. Plan 검토

| 항목 | 평가 | 코멘트 |
|---|---|---|
| 커밋 분할 | ✅ | 8커밋 — 토큰/a11y/responsive/state UI 분리 |
| 의존성 DAG | ✅ | 순차 1→2→3→4→5→6→7, 병렬 후보(3↔4) 명시 |
| 테스트 매핑 | ✅ | E2E #24 13 시나리오 회귀 검증 |
| 빌드 검증 | ✅ | `pnpm tsc` + `pnpm build` + Playwright + 3 profile (단일 환경 N/A) |
| 결정 항목 D1~D6 | ✅ | focus-ring 토큰, mobile-first vs desktop-first, state 컴포넌트 위치 등 사전 결정 |

## 3. UX 검토

- 9개 화면 자체는 #20, #21, #22 등에서 UX 검토 완료
- 본 PR은 polish — 새로운 인터랙션 패턴 추가 없음
- 모바일 반응형은 RealWorld 표준 레이아웃 따름 (사이드바 stack, meta wrap)
- a11y 보강 — 신규 사용자 그룹(키보드/스크린리더) 진입 가능해짐. 기존 마우스 사용자 영향 없음

## 4. 6단계 폴더링 충족

| 폴더 | 위치 | 충족 |
|---|---|---|
| feature 산출물 | `docs/features/design-fe-ui-polish/` | ✅ slug 접두 `design-` 정합 (manifest §3.2) |
| screenshots | `docs/features/design-fe-ui-polish/screenshots/` | ✅ (P12 이전 BEFORE 캡처) |
| ADR | `docs/planning/adr/0042-design-token-enforcement.md` | ⏳ P13에서 작성 |
| CHANGELOG | `docs/planning/CHANGELOG.md` §"Current Status" | ⏳ P13에서 갱신 |

## 5. frontmatter / Manifest 검증

- doc_type, version, status, author, date, gate, related 모든 필드 채움
- `validate-doc.sh`로 brief + contract 검증 PASS 확인 (plan/acceptance/risk/eng-review은 작성 후 추가 검증 예정)
- filename pattern `design-fe-ui-polish.{brief,contract,plan,acceptance,risk,eng-review}.md` — feature-*.schema.yaml의 `filename_pattern` 정합

## 6. 발견 사항 (3축 OX)

| Q | 답 | 처리 |
| --- | --- | --- |
| Q1: 본 plan은 백엔드/DB 영향이 있는가? | ❌ 없음 | FE-only 명시 |
| Q2: Breaking change 가능성 있는가? | ❌ 시각 변경만 — API/계약 불변 | 사용자 시점 의도된 변경, AC 명시 |
| Q3: 외부 의존성 추가가 있는가? | ❌ 없음 | React/CSS 표준 기능만 사용. Bootstrap CDN 유지 |
| Q4: 테스트 추가 없는 영역이 있는가? | ⚠️ presentational 컴포넌트 단위 테스트 없음 | E2E 13 시나리오로 회귀 검증 — 충분하다고 판단. follow-up 이슈 후보 |
| Q5: rollback 전략 검증 가능한가? | ✅ 단위 revert + 전체 revert 모두 안전 | DB 마이그레이션 없음 |
| Q6: 비목표 명확한가? | ✅ 5건 명시 (다크모드/컴포넌트 라이브러리/i18n/self-host/React Query) | 후속 이슈 후보로 docs-update에서 enqueue |

## 7. NEEDS-WORK 항목

- (낮은 우선순위) state 컴포넌트 단위 테스트 추가 — 후속 PR에서 React Testing Library 도입 시 처리
- (참고) Bootstrap 4 CDN의존 — 별도 이슈로 분리 (재현성·SLA 영향)
- (참고) 다크모드 토글 — 본 PR로 토큰 일관화가 완료되면 1주 내 후속 작업으로 가능
