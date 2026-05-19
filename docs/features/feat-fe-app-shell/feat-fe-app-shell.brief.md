---
doc_type: feature-brief
version: v0.1
date: 2026-05-19
status: Draft
issue: "#6"
mode: add
---

# feat-fe-app-shell — Intention Brief

## 의도 (1줄)

React Router 9개 라우트 + Header/Footer 레이아웃 + API client (axios + JWT interceptor) + Auth context — FE App Shell 일괄 구축.

## 범위

- HashRouter + 9개 라우트 (/, /login, /register, /settings, /editor, /editor/:slug, /article/:slug, /profile/:username, /profile/:username/favorites)
- Header: 비인증/인증 상태별 네비게이션
- Footer: RealWorld 표준
- API client: axios, baseURL /api, JWT Token interceptor
- AuthContext: JWT localStorage, login/logout/currentUser
- CSS Modules 스타일
