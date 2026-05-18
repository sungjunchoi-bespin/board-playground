---
doc_type: brief
gate: A
version: v1.0
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# Conduit (RealWorld) — Project Brief

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (analyst) | 초안 — RealWorld 스펙 기반 Project Brief 작성 |

## 1. 한 줄 정의

Medium.com 클론 블로그 플랫폼 "Conduit"를 RealWorld 스펙에 따라 풀스택으로 구현한다.

## 2. 배경 / 문제 정의

RealWorld 프로젝트는 Todo 앱 수준을 넘어 실제 서비스 수준의 복잡도를 갖춘 레퍼런스 애플리케이션이다. 150개 이상의 구현체가 존재하며, 통일된 API 스펙을 따르는 것이 핵심이다.

본 프로젝트는 RealWorld 공식 스펙(https://realworld-docs.netlify.app)을 RFP로 삼아 Conduit 애플리케이션을 구현한다. 주요 도메인:

- **사용자 인증**: 회원가입, 로그인, JWT 기반 인증
- **프로필**: 사용자 프로필 조회, 팔로우/언팔로우
- **아티클**: CRUD, 태그, 피드, 즐겨찾기, 페이지네이션
- **댓글**: 아티클에 대한 댓글 CRUD
- **태그**: 인기 태그 목록

## 3. 핵심 사용자 / 이해관계자

- **비회원 독자**: 글로벌 피드 열람, 태그 필터링, 아티클 조회
- **회원 작성자**: 아티클 작성/편집/삭제, 댓글 작성, 즐겨찾기
- **회원 팔로워**: 팔로잉 기반 개인 피드 구독
- **개발자(도입자)**: RealWorld 스펙 준수 여부 확인, 기술 스택 학습

## 4. 목표 (성공 정의)

| KPI | 측정 방법 | 목표값 | 달성 시점 |
|---|---|---|---|
| RealWorld API 스펙 준수율 | Postman/Newman 공식 테스트 suite 통과 | 100% | Sprint 3 종료 |
| 프론트엔드 라우트 구현율 | 9개 라우트 전수 검증 | 100% | Sprint 3 종료 |
| 단위 테스트 커버리지 | vitest/jest coverage report | 80% 이상 | Sprint 3 종료 |
| 통합 테스트 커버리지 | API endpoint 전수 검증 | 80% 이상 | Sprint 3 종료 |

## 5. 비목표 (Out of Scope)

- 실시간 알림 (WebSocket/SSE)
- 소셜 로그인 (OAuth)
- 검색 엔진 (Elasticsearch 등)
- 이미지 업로드 (프로필/아티클 이미지는 URL 입력 방식)
- 결제/구독 기능
- 모바일 앱 (웹 반응형만)
- 관리자 대시보드

## 6. 일정 (대략)

| Phase | 기간 | 산출 |
|---|---|---|
| 기획·설계 (Gate A~C) | 1일 | 01~13 산출물 |
| Sprint 1 — 인증·프로필 | 2~3일 | 회원가입/로그인/프로필 |
| Sprint 2 — 아티클·태그 | 2~3일 | CRUD/피드/태그/즐겨찾기 |
| Sprint 3 — 댓글·마무리 | 2일 | 댓글/E2E 테스트/배포 |

## 7. 리스크 (초기 식별)

| 리스크 | 영향 | 완화 |
|---|---|---|
| RealWorld API 스펙 변경 | 구현 재작업 | 2024/08/16 이후 List Articles body 미반환 변경 반영 |
| Bootstrap 4 테마 CDN 의존 | 스타일 깨짐 | 로컬 번들 또는 CDN fallback |
| JWT 보안 | 토큰 탈취 | httpOnly 쿠키 전환 검토 (v2) |

## 8. Open Questions

- 기술 스택 (프론트엔드·백엔드) 선택은 Gate C에서 결정
- DB 선택 (PostgreSQL vs SQLite) 확정 필요
- 배포 환경 (Vercel, Railway, Docker 등) 확정 필요
