---
doc_type: feature-plan
version: v0.1
date: 2026-05-19
status: Draft
issue: "#5"
mode: add
---

# feat-be-db-init — Implementation Plan

## Subtasks

1. **V1__init_schema.sql** — 7 테이블 DDL + 인덱스 + FK
2. **BaseEntity** — id, createdAt, updatedAt @MappedSuperclass
3. **application-test.yml 수정** — H2에서 Flyway가 문제없도록 ddl-auto: none 또는 flyway enabled
4. **Flyway 마이그레이션 테스트** — 앱 기동 시 테이블 생성 확인
5. **Spotless + 빌드 검증**
