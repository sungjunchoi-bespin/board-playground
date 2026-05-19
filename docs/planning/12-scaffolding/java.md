---
doc_type: scaffolding
gate: C
version: v1.0
date: 2026-05-19
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: []
  F-ID: []
  supersedes: null
---

# Conduit (RealWorld) Backend — Scaffolding

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-19 | Agent (architect) | 초안 -- Java 24 + Spring Boot 3.x + Hexagonal + DDD + Spring Modulith backend scaffolding |

## 1. 디렉토리 트리

Spring Boot + Hexagonal Architecture 기반 backend 단독 Gradle 프로젝트. `backend/` 디렉토리에 독립 배치.

```
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── src/main/java/com/conduit/
│   ├── ConduitApplication.java
│   ├── shared/
│   │   ├── security/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtTokenProvider.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ApiException.java
│   │   │   └── ErrorResponse.java
│   │   └── config/
│   │       ├── CorsConfig.java
│   │       └── OpenApiConfig.java
│   ├── user/
│   │   ├── domain/
│   │   │   ├── model/                    # User, Email, Password (Value Objects)
│   │   │   ├── port/in/                  # RegisterUserUseCase, LoginUserUseCase, GetUserUseCase, UpdateUserUseCase
│   │   │   ├── port/out/                 # UserRepository (outbound port)
│   │   │   └── exception/               # UserNotFoundException, DuplicateEmailException
│   │   ├── application/service/          # UserService (implements inbound ports)
│   │   └── adapter/
│   │       ├── in/web/                   # UserController, dto/, mapper/
│   │       └── out/persistence/          # UserJpaEntity, UserJpaRepository, UserPersistenceAdapter
│   ├── article/
│   │   ├── domain/
│   │   │   ├── model/                    # Article, Slug, Tag
│   │   │   ├── port/in/                  # CreateArticleUseCase, GetArticleUseCase, UpdateArticleUseCase, DeleteArticleUseCase, FavoriteArticleUseCase
│   │   │   ├── port/out/                 # ArticleRepository
│   │   │   └── exception/
│   │   ├── application/service/          # ArticleService
│   │   └── adapter/
│   │       ├── in/web/                   # ArticleController, dto/, mapper/
│   │       └── out/persistence/          # ArticleJpaEntity, ArticleJpaRepository, ArticlePersistenceAdapter
│   ├── comment/
│   │   ├── domain/
│   │   │   ├── model/                    # Comment
│   │   │   ├── port/in/                  # AddCommentUseCase, GetCommentsUseCase, DeleteCommentUseCase
│   │   │   ├── port/out/                 # CommentRepository
│   │   │   └── exception/
│   │   ├── application/service/          # CommentService
│   │   └── adapter/
│   │       ├── in/web/                   # CommentController, dto/, mapper/
│   │       └── out/persistence/          # CommentJpaEntity, CommentJpaRepository, CommentPersistenceAdapter
│   ├── profile/
│   │   ├── domain/
│   │   │   ├── model/                    # Profile
│   │   │   ├── port/in/                  # GetProfileUseCase, FollowUserUseCase, UnfollowUserUseCase
│   │   │   ├── port/out/                 # FollowRepository
│   │   │   └── exception/
│   │   ├── application/service/          # ProfileService
│   │   └── adapter/
│   │       ├── in/web/                   # ProfileController, dto/, mapper/
│   │       └── out/persistence/          # FollowJpaEntity, FollowJpaRepository, ProfilePersistenceAdapter
│   └── tag/
│       ├── domain/
│       │   ├── model/                    # Tag
│       │   ├── port/in/                  # GetTagsUseCase
│       │   ├── port/out/                 # TagRepository
│       │   └── exception/
│       ├── application/service/          # TagService
│       └── adapter/
│           ├── in/web/                   # TagController, dto/, mapper/
│           └── out/persistence/          # TagJpaEntity, TagJpaRepository, TagPersistenceAdapter
├── src/main/resources/
│   ├── application.yml                   # 공통 설정
│   ├── application-dev.yml               # dev profile
│   ├── application-stg.yml               # stg profile
│   ├── application-prod.yml              # prod profile
│   └── db/migration/                     # Flyway migration files
│       └── V1__init_schema.sql
└── src/test/java/com/conduit/
    ├── ConduitApplicationTests.java
    ├── user/                             # tests mirror main structure
    │   ├── domain/
    │   ├── application/
    │   └── adapter/
    ├── article/
    ├── comment/
    ├── profile/
    └── tag/
```

## 2. 패키지 명명 규칙

| 범위 | 규칙 | 예 |
|---|---|---|
| root package | `com.conduit` | `ConduitApplication.java` |
| module package | `com.conduit.{module}` | `com.conduit.user`, `com.conduit.article` |
| domain layer | `com.conduit.{module}.domain.{sublayer}` | `com.conduit.user.domain.model.User` |
| application layer | `com.conduit.{module}.application.service` | `com.conduit.user.application.service.UserService` |
| adapter-in layer | `com.conduit.{module}.adapter.in.web` | `com.conduit.user.adapter.in.web.UserController` |
| adapter-out layer | `com.conduit.{module}.adapter.out.persistence` | `com.conduit.user.adapter.out.persistence.UserPersistenceAdapter` |
| shared | `com.conduit.shared.{concern}` | `com.conduit.shared.security`, `com.conduit.shared.exception` |
| DTO | `com.conduit.{module}.adapter.in.web.dto` | `com.conduit.user.adapter.in.web.dto.UserResponse` |
| mapper | `com.conduit.{module}.adapter.in.web.mapper` | `com.conduit.user.adapter.in.web.mapper.UserMapper` |

- 패키지 명명은 `com.conduit.{module}.{layer}.{sublayer}` 형태로 통일한다.
- Hexagonal Architecture의 port/adapter 경계를 패키지 구조로 물리적으로 표현한다.
- Spring Modulith는 module 수준(`com.conduit.user`, `com.conduit.article` 등)에서 경계를 감지한다.

## 3. 디자인 패턴 결정

### Backend -- Hexagonal Architecture + DDD + Spring Modulith

**선택 패턴**: Hexagonal

**이유**: RealWorld 스펙은 5개 도메인 모듈(user, article, comment, profile, tag)을 가지며, 각 모듈의 도메인 로직이 인프라(DB, Web)와 독립적으로 테스트 가능해야 한다. Hexagonal Architecture는 domain -> port -> adapter 계층으로 관심사를 분리하여 다음 이점을 제공한다:

- **Domain 계층**: 순수 Java 객체로 비즈니스 규칙을 표현. 외부 프레임워크(Spring, JPA) 의존 없음.
- **Port 계층**: Use Case(inbound)와 Repository(outbound)를 인터페이스로 정의. domain이 adapter를 알지 못함.
- **Adapter 계층**: Web(Controller, DTO), Persistence(JPA Entity, Repository 구현)를 분리. 교체 가능.
- **Spring Modulith**: 모듈 간 의존 방향을 빌드 타임에 검증. 순환 의존 자동 차단.
- **DDD**: Value Object(Email, Password, Slug), Aggregate Root(User, Article), Domain Event 패턴을 활용.

## 4. 모듈 경계 (08-lld-module-spec와 fan-out)

| 모듈 | 역할 | 의존 (fan-out) | 비고 |
|---|---|---|---|
| `user.domain` | 사용자 도메인 모델, 포트 정의 | 없음 (순수 Java) | Aggregate Root: User |
| `user.application` | 비즈니스 로직 (Use Case 구현) | `user.domain` | UserService implements inbound ports |
| `user.adapter.in.web` | REST 엔드포인트 | `user.domain` (port/in), `shared.security` | Controller + DTO + Mapper |
| `user.adapter.out.persistence` | JPA 영속화 | `user.domain` (port/out), Spring Data JPA | JpaEntity + JpaRepository + PersistenceAdapter |
| `article.domain` | 게시글 도메인 모델, 포트 정의 | 없음 (순수 Java) | Aggregate Root: Article |
| `article.application` | 게시글 비즈니스 로직 | `article.domain`, `user.domain` (port/out) | ArticleService (사용자 조회 필요) |
| `article.adapter.in.web` | REST 엔드포인트 | `article.domain` (port/in) | ArticleController |
| `article.adapter.out.persistence` | JPA 영속화 | `article.domain` (port/out), Spring Data JPA | ArticlePersistenceAdapter |
| `comment.domain` | 댓글 도메인 모델 | 없음 | Comment |
| `comment.application` | 댓글 비즈니스 로직 | `comment.domain`, `article.domain` (port/out) | CommentService |
| `profile.domain` | 프로필/팔로우 도메인 모델 | 없음 | Follow 관계 |
| `profile.application` | 프로필 비즈니스 로직 | `profile.domain`, `user.domain` (port/out) | ProfileService |
| `tag.domain` | 태그 도메인 모델 | 없음 | Tag |
| `tag.application` | 태그 비즈니스 로직 | `tag.domain` | TagService |
| `shared.security` | 인증/인가 공통 | Spring Security, `user.domain` (port/out) | JWT 필터, SecurityConfig |
| `shared.exception` | 전역 예외 처리 | 없음 | GlobalExceptionHandler, ErrorResponse |
| `shared.config` | CORS, OpenAPI 공통 설정 | 없음 | CorsConfig, OpenApiConfig |

### fan-out 제약

- **의존 방향은 안쪽으로만**: adapter -> application -> domain. 역방향 의존 금지.
- **domain 계층은 외부 의존 없음**: Spring, JPA, 기타 프레임워크 import 금지.
- **모듈 간 의존**: application 계층에서 다른 모듈의 domain port/out만 의존 가능. adapter 간 직접 호출 금지.
- **shared 패키지**: 모든 모듈에서 import 가능하되, shared가 개별 모듈을 import하지 않음.
- **Spring Modulith** `@ApplicationModuleTest`로 모듈 경계 위반을 테스트 타임에 검증.

## 5. 빌드·실행

> **SoT**: 본 절이 backend 빌드/실행 명령의 정본 (ADR-0041). 루트 `LOCAL.md` ss3과 매 PR 동기 갱신.
> **호출 방식**: Gradle native script 직호출 (ADR-0041 -- wrapper 미사용).

| 명령 | 설명 |
|---|---|
| `./gradlew bootRun --args='--spring.profiles.active=dev'` | dev 프로필로 부팅 (port 8080) |
| `./gradlew bootRun --args='--spring.profiles.active=stg'` | stg 프로필로 부팅 (port 8080) |
| `./gradlew bootRun --args='--spring.profiles.active=prod'` | prod 프로필로 부팅 (port 8080) |
| `./gradlew build` | 전체 빌드 (컴파일 + 테스트) |
| `./gradlew test` | 전체 테스트 실행 |
| `./gradlew bootJar` | 실행 가능 JAR 생성 |

### dev (개발)

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
# port 8080, PostgreSQL localhost:5432/conduit_dev
# Flyway auto-migrate on startup (spring.flyway.enabled=true)
# Hibernate ddl-auto=validate
```

### stg (스테이징)

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=stg'
# port 8080, PostgreSQL host:5432/conduit_stg
# Flyway auto-migrate on startup
```

### prod (프로덕션)

```bash
cd backend
./gradlew bootJar
java -jar build/libs/backend-*.jar --spring.profiles.active=prod
# port 8080, PostgreSQL host:5432/conduit_prod
# Flyway auto-migrate on startup
```

### 공통 명령

```bash
# 의존성 다운로드 + 빌드
cd backend
./gradlew build

# 테스트만 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.conduit.user.application.service.UserServiceTest"

# JAR 생성
./gradlew bootJar
```

## 6. 환경 변수 / 설정 분리

Spring profiles 기반 설정 분리 (`application-{profile}.yml`). 시크릿은 prod/stg에서 secret manager 주입.

| 키 | dev | stg | prod | 노출 위치 |
|---|---|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/conduit_dev` | `jdbc:postgresql://host:5432/conduit_stg` | `jdbc:postgresql://host:5432/conduit_prod` | `application-{profile}.yml` (서버 전용) |
| `spring.datasource.username` | `conduit` | `conduit` | `(secret manager)` | `application-{profile}.yml` (서버 전용) |
| `spring.datasource.password` | `conduit` | `(secret manager)` | `(secret manager)` | `application-{profile}.yml` (서버 전용) |
| `conduit.jwt.secret` | `dev-jwt-secret-32chars-minimum!!` | `(secret manager)` | `(secret manager)` | `application-{profile}.yml` (서버 전용) |
| `conduit.jwt.expiration` | `7d` | `7d` | `7d` | `application-{profile}.yml` (서버 전용) |
| `server.port` | `8080` | `8080` | `8080` | `application-{profile}.yml` (서버 전용) |
| `conduit.cors.allowed-origins` | `http://localhost:5173` | `https://stg.conduit.example.com` | `https://conduit.example.com` | `application-{profile}.yml` (서버 전용) |
| `spring.flyway.enabled` | `true` | `true` | `true` | `application-{profile}.yml` (서버 전용) |

### 보안 주의

- `conduit.jwt.secret`은 `application-dev.yml`에만 평문 기재. stg/prod는 secret manager에서 환경 변수 주입.
- `spring.datasource.password`는 dev에 더미 값만 사용. stg/prod는 secret manager 주입.
- `application-{profile}.yml` 파일은 Git에 커밋하되, 시크릿 값은 placeholder만 기재.

## 7. 부팅 자산 (Runnability Assets)

| 자산 | 경로 (profile별) | 변경 trigger 이슈 유형 | 갱신 책임 |
|---|---|---|---|
| Spring profiles (dev) | `backend/src/main/resources/application-dev.yml` | 설정 변경 (환경 변수 추가, DB URL 변경) | 해당 이슈 담당 developer |
| Spring profiles (stg) | `backend/src/main/resources/application-stg.yml` | 설정 변경 | 해당 이슈 담당 developer |
| Spring profiles (prod) | `backend/src/main/resources/application-prod.yml` | 설정 변경 | 해당 이슈 담당 developer |
| 스키마 적용 (dev iteration) | N/A -- spring.flyway.enabled=true, bootRun이 곧 migrate. `backend/src/main/resources/db/migration/V*.sql` 추가 후 서버 재시작하면 자동 적용 | DB 스키마 변경 | developer (migration 파일 추가 시) |
| DB migrations (stg/prod release) | `backend/src/main/resources/db/migration/V*.sql` -- spring.flyway.enabled=true, 부팅 시 자동 적용 | DB 스키마 변경 (릴리스용) | developer (migration SQL 작성 후 커밋) |
| Gradle wrapper | `backend/gradlew`, `backend/gradle/wrapper/` | Gradle 버전 변경 | developer (gradlew --version 갱신 시) |
| Gradle build script | `backend/build.gradle.kts`, `backend/settings.gradle.kts` | 의존성 추가/변경/삭제 | developer |
| 설치/seed scripts | `./gradlew build` (의존성 다운로드 + 컴파일), seed는 Flyway migration 또는 별도 `data.sql` | 초기 세팅 변경 | developer |
| 부팅 명령 (dev) | `cd backend && ./gradlew bootRun --args='--spring.profiles.active=dev'` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| 부팅 명령 (stg) | `cd backend && ./gradlew bootRun --args='--spring.profiles.active=stg'` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| 부팅 명령 (prod) | `cd backend && ./gradlew bootJar && java -jar build/libs/backend-*.jar --spring.profiles.active=prod` | 빌드 스크립트 변경 | developer (ss5와 동기) |
| LOCAL.md | 루트 `LOCAL.md` | 부팅 절차/자산 변경 시 | developer (ADR-0040 -- 같은 PR에서 동기 갱신) |

### DB 스키마 전략 -- (b) 단일 메커니즘 (부팅 통합)

Spring Boot + Flyway integration을 채택한다. `spring.flyway.enabled=true` 설정으로 애플리케이션 부팅 시 자동으로 `db/migration/` 디렉토리의 migration 파일을 적용한다.

- **dev/stg/prod 공통**: bootRun(또는 JAR 실행) 시 Flyway가 자동으로 pending migration을 적용. 별도 `flyway migrate` CLI 호출 불필요.
- **migration 파일 작성**: `V{version}__{description}.sql` 형식 (예: `V1__init_schema.sql`, `V2__add_bio_column.sql`).
- **Hibernate ddl-auto**: `validate` 고정. Flyway가 스키마를 관리하므로 Hibernate의 자동 DDL 생성 비활성화.

## 8. 스타일링 솔루션

N/A -- BE-only. Frontend styling은 `typescript.md` ss8 참조.

| 항목 | 내용 |
|---|---|
| **솔루션** | N/A |
| **이유** | Backend 전용 scaffolding. UI/스타일링은 frontend 프로젝트(typescript.md)에서 관리. |
| **의존성** | N/A |
| **entrypoint 적용** | N/A |
| **디자인 토큰 매핑** | N/A |
