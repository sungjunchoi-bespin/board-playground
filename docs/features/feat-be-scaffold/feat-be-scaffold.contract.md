---
doc_type: feature-contract
version: v1.0
date: 2026-05-19
status: Draft
author: Agent (developer)
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# feat-be-scaffold — Change Contract

## 0. Referenced IDs
- R-ID: (none — infrastructure issue)
- F-ID: (none — infrastructure issue)
- Issue: #2
- Blocked-by: (none)
- Depends-on docs: 06-architecture, 07-hld, 12-scaffolding/java.md, 11-coding-conventions

## 1. Contract Before
Empty `backend/` directory (does not exist).

## 2. Contract After
- `backend/build.gradle.kts` — Spring Boot 3.x, Java 24, all dependencies declared
- `backend/settings.gradle.kts` — `rootProject.name = "backend"`
- `backend/gradlew`, `backend/gradlew.bat`, `backend/gradle/wrapper/` — Gradle Wrapper
- `backend/src/main/java/com/conduit/ConduitApplication.java` — Main class
- Hexagonal Architecture package structure for 5 modules (user, article, comment, profile, tag) + shared
- `backend/src/main/resources/application.yml` — common config
- `backend/src/main/resources/application-{dev,stg,prod}.yml` — 3 profile configs
- `backend/src/main/resources/db/migration/` — empty Flyway migration dir
- `backend/src/test/java/com/conduit/ConduitApplicationTests.java` — smoke test
- `./gradlew build` succeeds
- `./gradlew test` succeeds

## 3. Change Summary
| Item | Before | After |
|---|---|---|
| `backend/` directory | absent | Gradle project with Spring Boot 3.x |
| Package structure | N/A | `com.conduit.{module}.{domain,application,adapter}` Hexagonal layout |
| Build system | N/A | Gradle Kotlin DSL with wrapper |
| Profiles | N/A | dev/stg/prod application-*.yml |
| Tests | N/A | ConduitApplicationTests (context load smoke test) |
