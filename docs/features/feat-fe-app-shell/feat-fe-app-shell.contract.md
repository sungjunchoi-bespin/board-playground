---
doc_type: feature-contract
version: v0.1
date: 2026-05-19
status: Draft
issue: "#6"
mode: add
---

# feat-fe-app-shell — Change Contract

## §0 Referenced-IDs

- R-ID: (none — infra)
- F-ID: (none — infra)
- ADR: (none)
- Blocked-by: #3 (fe-scaffold, resolved)
- Supersedes: (none)

## §1 변경 요약

| 항목 | Before | After |
|---|---|---|
| Router | BrowserRouter, 1 route | HashRouter, 9 routes |
| Layout | 없음 | Header + Footer 레이아웃 |
| API client | 없음 | axios instance + JWT interceptor |
| Auth | 없음 | AuthContext + localStorage |
| Pages | placeholder only | 9개 라우트 placeholder pages |
