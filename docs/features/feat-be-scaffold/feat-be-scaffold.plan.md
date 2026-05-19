---
doc_type: feature-plan
version: v1.0
date: 2026-05-19
status: Draft
author: Agent (developer)
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# feat-be-scaffold — Implementation Plan

## Subtasks

### ST-1: Generate Gradle project skeleton
- `settings.gradle.kts` with `rootProject.name = "backend"`
- `build.gradle.kts` with Spring Boot 3.x plugin, Java 24 toolchain, all dependencies
- Gradle Wrapper (gradlew, gradlew.bat, gradle/wrapper/)
- Commit: `chore(backend): init Gradle project with Spring Boot 3.x deps #2`

### ST-2: Create main application + package structure
- `ConduitApplication.java` (main class)
- Hexagonal package tree for all 5 modules: user, article, comment, profile, tag
- `shared/` packages: security, exception, config
- Package-level `package-info.java` files for Spring Modulith module detection
- Commit: `chore(backend): create Hexagonal package structure + main class #2`

### ST-3: Configure Spring profiles (dev/stg/prod)
- `application.yml` — common settings
- `application-dev.yml` — dev PostgreSQL, JWT dev secret, CORS localhost:5173
- `application-stg.yml` — stg PostgreSQL, placeholder secrets
- `application-prod.yml` — prod PostgreSQL, placeholder secrets
- Empty `db/migration/` directory with `.gitkeep`
- Commit: `chore(backend): add Spring profile configs (dev/stg/prod) #2`

### ST-4: Add test + verify build
- `ConduitApplicationTests.java` — basic compilation/smoke test
- Run `./gradlew build` to verify
- Run `./gradlew test` to verify
- Commit: `chore(backend): add smoke test + verify build success #2`

## Risk
- Low: standard scaffolding, no external dependencies at runtime (DB required only at startup)
- Mitigation: tests run without DB by using `@SpringBootTest` with web environment mock

## Acceptance Criteria (from Issue #2)
Given empty project state, When `./gradlew build` executes, Then build success + Hexagonal Architecture package structure complete.
