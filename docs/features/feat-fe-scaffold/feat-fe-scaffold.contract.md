---
doc_type: feature-contract
version: v0.1
date: 2026-05-19
status: Draft
issue: "#3"
mode: add
---

# feat-fe-scaffold — Change Contract

## §0 Referenced-IDs

- R-ID: (none — infra chore)
- F-ID: (none — infra chore)
- ADR: (none)
- Blocked-by: (none)
- Supersedes: (none)

## §1 변경 요약

| 항목 | Before | After |
|---|---|---|
| `frontend/` 디렉토리 | 미존재 | React 18 + Vite + TS 프로젝트 |
| FE 빌드 | N/A | `pnpm build` 성공 |
| FE dev 서버 | N/A | `pnpm dev` (port 5173) |
| 스타일링 | N/A | CSS Modules + Bootstrap 4 CDN |
| 환경 변수 | N/A | `.env.{dev,stg,prod}.example` 3 profile |
| API 프록시 | N/A | `/api` → `http://localhost:8080` |

## §2 영향 분석

- 신규 디렉토리 생성만. 기존 `backend/` 무관.
- Breaking change: 없음 (신규 추가).
