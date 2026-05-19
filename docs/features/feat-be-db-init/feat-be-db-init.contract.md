---
doc_type: feature-contract
version: v0.1
date: 2026-05-19
status: Draft
issue: "#5"
mode: add
---

# feat-be-db-init — Change Contract

## §0 Referenced-IDs

- R-ID: (none — infra chore)
- F-ID: (none — infra chore)
- ADR: (none)
- Blocked-by: #2 (be-scaffold), #4 (be-shared-security) — resolved
- Supersedes: (none)

## §1 변경 요약

| 항목 | Before | After |
|---|---|---|
| DB 테이블 | 없음 | 7개 테이블 + 인덱스 + FK |
| Flyway 마이그레이션 | 파일 없음 | V1__init_schema.sql |
| JPA Entity | 없음 | BaseEntity 공통 클래스 |
| 테스트 인프라 | H2 only | Testcontainers PostgreSQL 추가 |
