---
doc_type: feature-plan
version: v0.1
date: 2026-05-19
status: Draft
issue: "#3"
mode: add
---

# feat-fe-scaffold — Implementation Plan

## Subtasks

1. **Vite + React 18 + TypeScript 프로젝트 초기화** — `pnpm create vite` 또는 수동 package.json
2. **의존성 추가** — react-router-dom
3. **vite.config.ts** — proxy /api → localhost:8080
4. **tsconfig.json** — strict, path alias @/ → src/
5. **디렉토리 구조** — src/{components,pages,hooks,api,types,styles}/ 골격
6. **index.html** — Bootstrap 4 CDN, Google Fonts, Ionicons
7. **styles/global.css** — 디자인 토큰 CSS custom properties
8. **src/main.tsx + src/app.tsx** — 진입점 + 라우터 기본
9. **.env.{dev,stg,prod}.example** — VITE_API_URL 3 profile
10. **빌드 검증** — pnpm install && pnpm dev && pnpm build
