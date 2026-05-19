---
doc_type: feature-brief
version: v0.1
date: 2026-05-19
status: Draft
issue: "#3"
mode: add
---

# feat-fe-scaffold — Intention Brief

## 의도 (1줄)

React 18 + Vite + TypeScript 기반 프론트엔드 프로젝트 스캐폴딩 — `frontend/` 디렉토리에 독립 배치, CSS Modules + Bootstrap 4 CDN 스타일링, 3 profile 환경 변수 분리.

## 배경

Sprint 0 인프라 이슈. Backend 스캐폴딩(#2) 완료 후 Frontend 진입점 구축. 12-scaffolding/typescript.md SoT 기반.

## 범위

- `frontend/` 디렉토리 생성 (pnpm standalone, workspace 없음)
- React 18, TypeScript 5, Vite 5, react-router-dom
- vite.config.ts — proxy /api → localhost:8080
- tsconfig.json — strict, path alias (@/)
- index.html — Bootstrap 4 CDN, Google Fonts (Source Sans Pro), Ionicons CDN
- CSS Modules + global.css 디자인 토큰
- .env.{dev,stg,prod}.example 3 profile
- pnpm dev / pnpm build 성공
