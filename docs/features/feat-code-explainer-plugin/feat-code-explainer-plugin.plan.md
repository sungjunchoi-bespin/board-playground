---
doc_type: feature-plan
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

# code-explainer 플러그인 도입 — Implementation Plan

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v0.1 | 2026-05-20 | woosung.ahn | 초안 (scaffold-doc.sh 생성) |

## 1. 커밋 시퀀스 (DAG)

| # | 커밋 | 영향 파일 | 테스트 추가 | 회귀 위험 |
| --- | --- | --- | --- | --- |
| C1 | `feat(plugin): add code-explainer plugin scaffolding #54` | `code-explainer-plugin/.claude-plugin/plugin.json`, `code-explainer-plugin/LICENSE`, `code-explainer-plugin/README.md`, `code-explainer-plugin/config.json` | N/A (외부 도구) | 없음 — 신규 디렉토리 추가만 |
| C2 | `feat(plugin): add SKILL.md with Discussion+Wiki dual sink #54` | `code-explainer-plugin/skills/code-explainer/SKILL.md` | N/A | 없음 — Phase 3.5 Wiki 적재 절차 포함, Phase 0.4 사전 조건 분리 |
| C3 | `docs(feature): add feat-code-explainer-plugin artifacts #54` | `docs/features/feat-code-explainer-plugin/*.md` (6개) | N/A | 없음 |
| C4 | `feat(plugin): run plugin against frontend → adopt Discussion+Wiki #54` | (Wiki repo 측 push, board-playground main에는 파일 변경 없음) | N/A (검증은 manual + Discussion/Wiki URL 확인) | 없음 |

> 본 작업은 board-playground 빌드/런타임 코드 변경이 없으므로 C1·C2·C3는 한 커밋으로 묶어도 안전. 다만 변경 의도를 분리 추적하기 위해 권고대로 분리한다.

## 2. 의존성 그래프

```
C1 (scaffolding 5 files)
   └─ C2 (SKILL.md with Wiki Phase) ── depends on C1 (.claude-plugin/plugin.json 메타)
        └─ C3 (feature artifacts 6 files) ── depends on contract.md §0 Referenced-IDs 결정
             └─ C4 (run plugin) ── depends on C1+C2 plugin 준비 + Wiki repo 초기화 (선행 충족됨, HEAD=a00249481)
```

## 3. 테스트 매핑

| 커밋 | 테스트 추가 위치 | 시나리오 |
| --- | --- | --- |
| C1 | (없음) | 정적 파일 추가만. JSON 문법 유효성은 `jq`로 검증. |
| C2 | (없음) | SKILL.md는 markdown + 절차 문서. 검증은 C4 실행 결과로 갈음. |
| C3 | `bash .claude/scripts/validate-doc.sh docs/features/feat-code-explainer-plugin/*.md` | schema-level validation 6개 문서 PASS |
| C4 | manual — Discussion URL ≥ 37개 생성, Wiki에서 `Home.md`/`_Sidebar.md`/`L1-`/`L2-`/`L3-*` 페이지 push 확인 | end-to-end 적재 검증 |

## 4. 빌드·실행 검증 단계

본 PR은 board-playground의 frontend/backend 빌드에 영향이 없으므로 dev/stg/prod 3 profile 부팅 검증은 **N/A**. 단, 변경 사실을 명시적으로 검증:

```bash
# 1. JSON 메타파일 유효성
jq . code-explainer-plugin/.claude-plugin/plugin.json
jq . code-explainer-plugin/config.json

# 2. SKILL.md frontmatter + 절차 marker 존재 확인
grep -q "^name: code-explainer" code-explainer-plugin/skills/code-explainer/SKILL.md
grep -q "Phase 3.5: GitHub Wiki 적재" code-explainer-plugin/skills/code-explainer/SKILL.md
grep -q "Phase 0.4: Wiki" code-explainer-plugin/skills/code-explainer/SKILL.md || \
  grep -q "4. Wiki 활성화" code-explainer-plugin/skills/code-explainer/SKILL.md

# 3. 기존 frontend 빌드가 영향받지 않음 (sanity)
cd frontend && pnpm tsc --noEmit && cd ..

# 4. feature 산출 문서 schema 검증
for f in docs/features/feat-code-explainer-plugin/*.md; do
  bash .claude/scripts/validate-doc.sh "$f" || echo "FAIL: $f"
done

# 5. end-to-end 적재 검증 (수동 실행 후 URL 확인)
# - https://github.com/sungjunchoi-bespin/board-playground/discussions?discussions_q=label%3AL1+label%3Aboard-playground
# - https://github.com/sungjunchoi-bespin/board-playground/wiki
```

### 3 profile 부팅 검증 (ADR-0037 v1.1 + ADR-0040)

**N/A** — 본 PR diff는 `code-explainer-plugin/` + `docs/features/feat-code-explainer-plugin/` 만 포함하며 다음을 모두 만족:

- `frontend/`, `backend/` 소스 변경 없음
- `frontend/package.json`, `backend/build.gradle` 등 의존성 정의 변경 없음
- `.env.{dev,stg,prod}.example`, DB migrations, lockfile 변경 없음
- `12-scaffolding/{java,typescript}.md` §5 빌드 명령 변경 없음
- `LOCAL.md` §3 profile별 부팅 명령 변경 없음

따라서 dev/stg/prod 부팅 검증을 생략하며, PR 본문 6번째 축에 본 사유를 명시한다.

## 5. 점진 합의 / 결정 발생 항목

- (해소) Wiki 사전 조건 미충족 시 BLOCK vs soft-skip → **soft-skip** (Discussion은 계속 진행, AI 게이트에 영향 없음)
- (해소) 페이지 명명 규칙 — 슬래시 vs 하이픈 → **하이픈** (`L2-frontend-src-components.md`)
- (해소) 기존 페이지 처리 — Wiki는 git이라 별도 백업 절차 없음. Discussion은 코멘트 백업.
- (해소) frontend vs backend 우선 → frontend (파일 수 적어 파일럿으로 적합)
