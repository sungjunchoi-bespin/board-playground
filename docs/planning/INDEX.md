---
doc_type: index
version: v0.1
status: Draft
author: gen-index.sh
date: 2026-05-19
gate: operations
related: { R-ID: [], F-ID: [], supersedes: null }
---

# Conduit (RealWorld) — Planning Index

> `/flow-new-project` 4 Phase 산출 전체 인덱스.

| NN | 폴더 | 산출 | Gate | 상태 |
|---|---|---|---|---|
| 01 | 01-project-brief | Project Brief | A | Draft v1.1 |
| 02 | 02-feasibility | Feasibility Study | A | Draft v1.1 |
| 03 | 03-user-scenarios | User Scenarios | B | Draft v1.0 |
| 04 | 04-srs | SRS (Software Requirements Specification) | B | Draft v1.1 |
| 05 | 05-prd | PRD (Product Requirements Document) | B | Draft v1.1 |
| 06 | 06-architecture | Architecture | C | Draft v1.1 |
| 07 | 07-hld | HLD (High-Level Design) | C | Draft v1.1 |
| 08 | 08-lld-module-spec | Module Specification (LLD) | C | Draft v1.1 |
| 09 | 09-lld-api-spec | API Specification (LLD) | C | Draft v1.1 |
| 10 | 10-lld-screen-design | Screen Design (LLD) | C | Draft v1.0 |
| 11 | 11-coding-conventions | Coding Conventions | C | Draft v1.1 |
| 12 | 12-scaffolding | Scaffolding (java.md + typescript.md) | C | Draft v1.0/v1.1 |
| 13 | 13-test-design | Test Design | C | Draft v1.1 |
| 14 | 14-wbs | WBS (Work Breakdown Structure) | ops | Draft v0.1 |
| 15 | 15-risk | Risk Register | ops | Draft v0.1 |

## 기술 스택

- **Frontend**: React 18 + TypeScript + Vite + CSS Modules + Bootstrap 4 CDN
- **Backend**: Java 24.x + Spring Boot 3.x + Hexagonal/DDD/Spring Modulith + PostgreSQL + Flyway
- **보안**: Spring Security 6.x + JWT (jjwt)
- **API Docs**: Springdoc OpenAPI 2.x (Swagger UI)

## WBS 요약

- 5 Sprints (Sprint 0~4), 25 Issues
- R-ID 25개 + F-ID 10개 100% 매핑
- 리스크 7건 (High 0, Medium 5, Low 2)
