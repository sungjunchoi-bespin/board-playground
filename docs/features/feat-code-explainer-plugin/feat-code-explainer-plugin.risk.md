---
doc_type: feature-risk
version: v0.1 (Draft)
status: Draft
author: woosung.ahn@bespinglobal.com
date: 2026-05-20
gate: feature
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# code-explainer 플러그인 도입 — Feature Risk

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v0.1 | 2026-05-20 | woosung.ahn | 초안 (scaffold-doc.sh 생성) |

## 1. 본 변경의 리스크

| RISK-ID | 제목 | 영향(1~5) | 가능성(1~5) | 등급 |
| --- | --- | --- | --- | --- |
| R1 | Wiki repo 인증 실패로 push 누락 | 2 | 2 | **Low** |
| R2 | LLM 분석 결과의 사실 오류 (잘못된 함수 설명) | 2 | 3 | **Low** |
| R3 | Discussion ~37개·Wiki ~37 페이지 일괄 적재로 노이즈 알림 폭주 | 1 | 2 | **Low** |
| R4 | 플러그인 디렉토리가 IDE/빌드 도구에 잘못 인식 (예: TypeScript tsconfig include) | 3 | 1 | **Low** |
| R5 | config.json `webhook_url` 누설 (이번 PR은 빈 값이라 해당 없음) | 4 | 1 | **Low** |

> 영향 × 가능성 ≥ 9 (High 등급)에 해당하는 리스크 없음.

## 2. 리스크 상세

### R1 — Wiki push 실패

- **발현 조건**: `gh auth setup-git` 실패, 또는 wiki repo가 다시 비초기화 상태로 돌아감
- **완화**: SKILL.md Phase 3.5에서 Wiki 실패는 soft-fail 처리 — Discussion 적재는 계속, 사용자에게 안내만 출력. PR 본문에 "Wiki: 스킵됨 (사유)" 명시.
- **탐지**: 적재 직후 `git ls-remote https://github.com/sungjunchoi-bespin/board-playground.wiki.git`로 최신 커밋 확인

### R2 — LLM 사실 오류

- **발현 조건**: Claude가 코드를 잘못 해석하여 함수 의도/리뷰 기준을 오기재
- **완화**: 학습 문서임을 Wiki Home / Discussion 본문에 명시. "이 문서는 LLM 자동 생성. 사실 확인은 소스 파일 우선". 발견 시 재실행으로 갱신.
- **탐지**: 휴먼 게이트(D-06 2단)에서 randomly 1~2개 L3 페이지 샘플링하여 코드와 대조

### R3 — 알림 폭주

- **발현 조건**: Repository watchers에게 Discussion 신규 생성 N+ 알림 동시 발송
- **완화**: 한 번에 ~37개라 발송 빈도는 1회. 후속 backend 도입 시(108개) PR 본문에 적재 시작 시각 안내.
- **탐지**: 사후 (사용자 피드백)

### R4 — IDE/빌드 도구 인식

- **발현 조건**: tsconfig.json `include`에 `**/*.ts`가 있으면 `code-explainer-plugin/`이 포함될 가능성
- **완화**: frontend/tsconfig.json은 `"include": ["src"]`로 제한적. backend tsconfig 없음. 즉, 영향 없음.
- **탐지**: AC-6 (`pnpm tsc --noEmit`) 회귀 인수로 검증

### R5 — webhook URL 누설

- **발현 조건**: 추후 누군가 `webhook_url`을 채우고 그대로 커밋
- **완화**: 본 PR에서는 `""` 빈 값. SKILL.md Tips와 README에 "webhook_url은 비공개 환경변수로 관리 권장" 후속 작업 항목으로 메모.
- **탐지**: pre-commit hook(`check-secrets`) 활용 — 현재 settings.json PreToolUse 훅이 시크릿 파일 패턴을 차단함

## 3. High 등급 단계적 롤아웃

해당 없음 — High 등급 리스크 0건. 단일 PR로 일괄 적용.

## 4. 데이터 영속성 변경

- **board-playground DB**: 영향 없음 (`backend/src/main/resources/db/migration/` 미접촉)
- **GitHub Discussions**: 신규 페이지 ~37개 생성. 기존 페이지 미접촉 (라벨 `board-playground`로 분리됨).
- **GitHub Wiki**: 신규 페이지 + `Home.md`, `_Sidebar.md` push. 초기화 시 만들었던 사용자 Home 페이지가 덮어쓰여질 수 있음 — **사용자 확인 필요**.

> **사용자 확인 요망**: Wiki 초기화 시 만든 첫 페이지(예: `Home`)가 플러그인의 `Home.md`로 덮어쓰여지는 것이 의도와 일치하는지 머지 전 확인.

## 5. 15-risk.md 갱신 항목

본 변경의 리스크 등급이 모두 Low이고 외부 도구 도입이므로 프로젝트 전역 리스크 레지스터(`docs/planning/15-risk/15-risk.md`) 갱신은 불요.
