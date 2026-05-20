---
doc_type: feature-brief
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

# code-explainer 플러그인 도입 — Feature Brief

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v0.1 | 2026-05-20 | woosung.ahn | 초안 (scaffold-doc.sh 생성) |

## 1. 한 줄 의도

board-playground 신규 합류자가 React/Spring 코드를 빠르게 학습할 수 있도록 **L1/L2/L3 3단계 학습 문서**를 자동 생성하여 GitHub Discussion + Wiki에 동시 적재하는 외부 플러그인을 도입한다.

## 2. 사용자 가치

| 페르소나 | 현재 | 가치 |
|---|---|---|
| 신규 합류자 | `04-srs` / `06-architecture`는 의도/요구 중심이라 "어떤 파일의 어떤 함수가 무엇을 하는지" 찾기 어려움 | 파일 단위 학습 문서가 Discussion + Wiki에 자동 적재되어 위키 사이드바·라벨 필터로 탐색 가능 |
| 코드리뷰어 | PR 본문만으로 변경 맥락 파악 | 변경 파일에 해당하는 L3 페이지로 즉시 이동 가능 |
| 운영자 | 별도 문서 유지 부담 | 코드 변경 시 플러그인 재실행으로 문서 자동 갱신 (Wiki는 git history로 버전 누적, Discussion은 코멘트로 백업) |

## 3. 현재 상태 → 변경 후 상태

| 측면 | 현재 | 변경 후 |
| --- | --- | --- |
| 학습 진입점 | `README.md` + `docs/planning/01-project-brief` | + `https://github.com/sungjunchoi-bespin/board-playground/wiki` (Home + Sidebar 자동 생성) |
| 코드 단위 설명 | 없음 (코드 직접 읽기) | L3 페이지 28개 (frontend/src TS/TSX) |
| 모듈 단위 설명 | `06-architecture` HLD 일부 | L2 페이지 ~8개 (디렉토리별 책임·파일·디자인 패턴) |
| 전체 흐름 | `06-architecture` | L1 1개 (읽기 순서 가이드 + 용어 사전 + 환경/설정 개요) |
| sink | 없음 | Discussion (라벨 분류) + Wiki (사이드바 네비) 동시 |

## 4. 모드 자동 감지 결과

- 입력 자연어 / 라벨 분석:
  - `type:bug` 라벨/에러 키워드: **없음**
  - UI/token/시각 키워드: **없음**
  - 기존 동작 변경 / breaking 키워드: **없음**
- ADR-0032 §2.1 — 부정 시그널 0건 → **mode=add** 자동 결정
- 자동 결정이며 사용자 질문 없이 진행 (ADR-0032 §2.2)

## 5. 영향 범위

| 영역 | 변경 |
|---|---|
| `code-explainer-plugin/` (신규) | 플러그인 본체 5파일 추가 |
| `code-explainer-plugin/skills/code-explainer/SKILL.md` | Phase 0.4 (Wiki 사전 조건) + Phase 3.5 (Wiki 적재) 추가, 파이프라인/체크리스트/Tips 갱신 |
| `code-explainer-plugin/README.md` | Discussion + Wiki 동시 적재 명시, 구조 표 갱신 |
| `code-explainer-plugin/config.json` | `discussion_repo_owner=sungjunchoi-bespin`, `discussion_repo_name=board-playground` 채움 |
| 기존 `frontend/`, `backend/`, `docs/planning/` 등 | **변경 없음** (플러그인은 외부 도구, 빌드 의존 없음) |
| 외부 영향 | GitHub Discussions에 ~37 항목 신규 생성, Wiki에 ~37 페이지 + Home/_Sidebar push |

## 6. 비목표

- backend(108 파일) 적재 — 후속 이슈로 분리
- 다국어 출력 (한국어만)
- 자동 재실행 스케줄러 (수동 `/code-explainer` 실행)
- Google Chat 알림 (`webhook_url` 빈 값으로 스킵)
- 플러그인의 CI 통합 (이번 PR은 도입만)

## 7. Open Questions

- (해소됨) Wiki 적재 방식 → wiki repo clone + push 결정
- (해소됨) 같은 저장소 vs 별도 저장소 → Discussion과 동일 저장소
- (해소됨) 항상 켜기 vs 토글 → 항상 켜기, 사전 조건 실패 시 Wiki만 soft-skip
