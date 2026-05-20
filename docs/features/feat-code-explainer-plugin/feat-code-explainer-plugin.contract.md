---
doc_type: feature-contract
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

# code-explainer 플러그인 도입 — Change Contract

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v0.1 | 2026-05-20 | woosung.ahn | 초안 (scaffold-doc.sh 생성) |

## 0. 참조 정본 ID (Referenced-IDs)

| 종류 | 정본 위치 | 영향 ID |
| --- | --- | --- |
| Requirement | (none) | — 본 변경은 학습 보조 도구 도입으로 기능 요구사항(R-/F-)을 새로 추가하거나 변경하지 않음 |
| ADR | `docs/planning/adr/` | (none) — 외부 플러그인 추가는 board-playground 빌드/런타임 의존을 변경하지 않으므로 ADR 불요 |
| 코딩 컨벤션 | `docs/planning/11-coding-conventions/11-coding-conventions.md` | (none) — 본 PR diff는 `code-explainer-plugin/` 디렉토리만 추가, 기존 컨벤션 적용 대상이 아님 |
| 스카폴딩 | `docs/planning/12-scaffolding/{java,typescript}.md` | (none) — 빌드 명령 영향 없음 |
| 시나리오/시험 | `docs/planning/13-test-design/13-test-design.md` | (none) — 본 플러그인은 board-playground 빌드/테스트 파이프라인 외부에서 실행되며, 별도 테스트 catalog 갱신 불요 |

## 1. 변경 의도

`code-explainer-plugin/` 디렉토리를 board-playground 루트에 신규 추가하여, Claude Code에서 `/code-explainer <path>` 호출 시 frontend(또는 추후 backend)의 학습 문서를 자동 생성하고 GitHub Discussion + Wiki 두 sink에 동시 적재한다.

## 2. Before / After

| 항목 | Before | After |
| --- | --- | --- |
| 플러그인 디렉토리 | 없음 | `code-explainer-plugin/` (5 파일 + .claude-plugin 메타) |
| Wiki sink | 미사용 | `Home.md`, `_Sidebar.md`, `L1-<프로젝트>.md`, `L2-<dir>.md`, `L3-<dir>-<file>.md` 자동 push |
| Discussion sink | 미사용 | General 카테고리에 L1/L2/L3 + 프로젝트명 Label로 분류 적재 |
| 학습 진입점 (Wiki) | 비어있음 | `https://github.com/sungjunchoi-bespin/board-playground/wiki` Home에서 L1 → L2 → L3 탐색 가능 |
| frontend 코드 | 변경 없음 | 변경 없음 (플러그인은 외부 도구) |
| backend 코드 | 변경 없음 | 변경 없음 |
| `package.json` / `pom.xml` / `build.gradle` | 변경 없음 | 변경 없음 (빌드 의존 추가 없음) |
| 3 profile boot (dev/stg/prod) | 영향 없음 | 영향 없음 (LOCAL.md / 12-scaffolding 갱신 불요) |

## 3. 호출자·의존자 (Call Sites)

| 위치 | 영향 | 조치 |
| --- | --- | --- |
| `frontend/` 빌드 (`pnpm build`) | 없음 | 검증 — `tsc` / `vite build`가 `code-explainer-plugin/`을 스캔하지 않음 (frontend/tsconfig.json 범위 외) |
| `backend/` 빌드 (`./gradlew build`) | 없음 | 검증 — `code-explainer-plugin/`은 `backend/` 외부 |
| CI 워크플로 (`.github/workflows/`) | 없음 | 영향 없음 (CI는 빌드만 수행, plugin은 로컬 실행) |
| `.gitignore` | 변경 없음 | plugin 디렉토리는 git tracked |
| GitHub Discussions / Wiki | 신규 콘텐츠 적재 | 사용자 트리거(`/code-explainer`)로만 적재 |

## 4. Backward Compatibility

- **완전 호환**. 본 변경은 기존 코드/빌드/실행/CI 경로를 일절 건드리지 않는다.
- 플러그인 미사용자(`/code-explainer`를 호출하지 않는 사용자)는 디렉토리 1개의 존재 외 어떤 영향도 받지 않는다.
- Discussions/Wiki에 신규 페이지가 생기지만, 기존 페이지(없음)를 덮어쓰지 않는다.
- breaking change 없음 → ADR 불요 (mode=add 정책)

## 5. Rollback 전략

| 시나리오 | 조치 |
|---|---|
| 플러그인 도입 자체 롤백 | `git revert` 1회 — `code-explainer-plugin/` 디렉토리 통째 제거. 빌드/런타임 영향 0. |
| Wiki 페이지만 제거 | wiki repo를 클론해서 해당 파일 삭제 후 push. Discussion은 별도. |
| Discussion 항목만 제거 | GitHub UI에서 일괄 삭제, 또는 GraphQL mutation으로 batch delete. |
| 둘 다 제거 | 위 두 단계 병행. plugin 코드 자체는 유지 가능 (idempotent 재실행 시 다시 생성). |

## 6. 비목표

- backend 적재 (별도 이슈)
- 영문/다국어 문서 출력
- 자동 스케줄링 (cron, GitHub Actions 등)
- Google Chat 알림 (`webhook_url`을 채우는 후속 작업)
- 플러그인 자체의 단위 테스트 추가 (외부 도구이므로 board-playground 테스트 catalog 외)
- 02-test-catalog 또는 13-test-design 갱신 (테스트 영향 없음)
