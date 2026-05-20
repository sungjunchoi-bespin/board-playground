---
doc_type: feature-acceptance
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

# code-explainer 플러그인 도입 — Acceptance Criteria

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v0.1 | 2026-05-20 | woosung.ahn | 초안 (scaffold-doc.sh 생성) |

## 1. 인수 기준 (Given/When/Then)

### AC-1. 플러그인 디렉토리 존재

- **Given** main 브랜치에서 `code-explainer-plugin/` 디렉토리가 없는 상태
- **When** 본 PR이 머지됨
- **Then** `code-explainer-plugin/{SKILL.md(via skills/code-explainer/), config.json, README.md, .claude-plugin/plugin.json, LICENSE}` 5개 파일이 존재

### AC-2. SKILL.md에 Wiki Phase 포함

- **Given** `code-explainer-plugin/skills/code-explainer/SKILL.md` 존재
- **When** 파일을 grep
- **Then** `Phase 3.5: GitHub Wiki 적재` 헤더와 `4. Wiki 활성화 + 초기화 확인` 헤더가 둘 다 존재

### AC-3. config.json이 board-playground로 설정

- **Given** `code-explainer-plugin/config.json` 존재
- **When** `jq` 파싱
- **Then** `discussion_repo_owner == "sungjunchoi-bespin"` AND `discussion_repo_name == "board-playground"`

### AC-4. frontend 적재 — Discussion

- **Given** Wiki/Discussion 사전 조건 충족 + `/code-explainer frontend/` 실행 완료
- **When** GitHub Discussions 페이지 확인
- **Then** L1 1개 + L2 ≥ 8개 + L3 = 28개 = 총 **≥ 37개** Discussion이 `label:board-playground` 필터로 보임

### AC-5. frontend 적재 — Wiki

- **Given** AC-4와 같은 실행 완료
- **When** `https://github.com/sungjunchoi-bespin/board-playground/wiki` 확인
- **Then** `Home`, `_Sidebar`, `L1-board-playground`, `L2-frontend-src-*` (≥ 8개), `L3-frontend-src-*-*` (= 28개) 페이지가 존재

### AC-6. 기존 빌드 무영향

- **Given** main + 본 PR diff 적용 상태
- **When** `cd frontend && pnpm tsc --noEmit && pnpm build` 실행
- **Then** exit code 0, 새로운 에러/경고 0건

### AC-7. feature 산출 문서 schema-validate

- **Given** `docs/features/feat-code-explainer-plugin/*.md` 6개 파일
- **When** `.claude/scripts/validate-doc.sh` 각각 실행
- **Then** 6/6 PASS

## 2. Definition of Done (D-06)

### 1단 — AI 게이트 (PR 생성 전)

- [ ] AC-1 ~ AC-7 모두 PASS
- [ ] `docs/features/feat-code-explainer-plugin/*.md` 6개 schema-validate PASS
- [ ] PR 본문 Test Plan 4블록 포함 (`docs/features/feat-code-explainer-plugin/feat-code-explainer-plugin.ai-qa.md` 또는 PR body에 직접 기재)
- [ ] 6번째 축(3 profile 부팅) — **N/A 사유 명시** (소스/의존성/profile 자산 무변경)
- [ ] 5번째 축(stylesheet 적용) — **N/A 사유 명시** (`ui_changed=false`, frontend/ 디렉토리 미접촉)

### 2단 — 휴먼 게이트 (머지 전)

- [ ] `tested` 라벨 부착
- [ ] Approve ≥ 1
- [ ] CI green (existing build/test workflow)
- [ ] Discussion 인덱스 URL + Wiki Home URL이 PR 본문에 첨부됨

## 3. 비기능 인수

| 축 | 기준 | 측정 |
|---|---|---|
| 성능 | 플러그인이 board-playground 런타임에 영향 0 | 빌드 시간 변화 ≤ 1% (외부 디렉토리이므로 자동) |
| 보안 | 시크릿/토큰 커밋 없음 | `code-explainer-plugin/config.json` `webhook_url=""` 비어있음 확인 |
| 호환성 | 기존 frontend/backend/CI 빌드 무회귀 | AC-6으로 검증 |
| 운영 | `git revert` 1회로 100% 롤백 가능 | Rollback 시나리오로 검증 |

## 4. 회귀 인수

- [ ] `pnpm tsc --noEmit` (frontend) — 본 PR 이전 대비 에러 변화 0
- [ ] `pnpm build` (frontend) — 본 PR 이전 대비 출력 변화 0
- [ ] `./gradlew build` (backend) — 본 PR 이전 대비 결과 변화 0 (`backend/` 디렉토리 미접촉)
- [ ] 기존 docs/planning 문서 무변경 — `git diff main -- docs/planning/` 결과 0줄
