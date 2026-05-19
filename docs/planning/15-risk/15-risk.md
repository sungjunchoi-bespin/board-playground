---
doc_type: risk
version: v0.1
status: Draft
author: sungjun.choi@board-playground.dev
date: 2026-05-19
gate: operations
related:
  R-ID: [R-F-01, R-F-02, R-F-03, R-F-04, R-F-05, R-F-06, R-F-07, R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-14, R-F-15, R-F-16, R-F-17, R-F-18, R-F-19, R-N-01, R-N-02, R-N-03, R-N-04, R-N-05, R-N-06]
  F-ID: [F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10]
  supersedes: null
---

# Conduit (RealWorld) — Risk Register

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v0.1 | 2026-05-19 | sungjun.choi | 초안 작성 - 7건 리스크 식별 |

## 1. 리스크 일람

| RISK-ID | 제목 | 영향(1~5) | 가능성(1~5) | 등급 | 영향 받는 Sprint/Issue | 대응 |
|---|---|---|---|---|---|---|
| RISK-01 | Java 24 라이브러리 호환성 | 3 | 3 | Medium | Sprint 0 (scaffolding, 의존성 확정) | Java 21 LTS fallback 준비, Sprint 0 초기에 전 의존성 빌드 검증 |
| RISK-02 | PostgreSQL 로컬 환경 설치 부담 | 2 | 3 | Medium | Sprint 0 (DB init, Docker Compose) | Docker Compose 제공, LOCAL.md에 설치 가이드 명시 |
| RISK-03 | Hexagonal Architecture 학습 곡선 | 3 | 2 | Medium | Sprint 0~1 (구조 템플릿, 첫 모듈 구현) | 11-coding-conventions에 의존 방향 규칙 명시, Sprint 0에 구조 템플릿 제공 |
| RISK-04 | Spring Modulith 미성숙 | 2 | 2 | Low | Sprint 0 (모듈 경계 설정) | @ApplicationModuleTest만 사용, 핵심 기능은 Spring Boot native로 대체 가능 |
| RISK-05 | RealWorld Token 인증 스펙 비표준 | 3 | 2 | Medium | Sprint 1 (인증 구현, F-01~03) | JwtAuthFilter에서 Token prefix 파싱 구현, 통합 테스트에서 검증 |
| RISK-06 | CDN 외부 의존성 | 2 | 1 | Low | Sprint 1~3 (프론트엔드 전반) | 로컬 번들 fallback, 오프라인 시 시스템 폰트 대체 |
| RISK-07 | Flyway 마이그레이션 충돌 | 3 | 2 | Medium | Sprint 1~3 (스키마 변경 시점) | Sprint 0에서 V1__init 단일 파일로 전체 스키마 생성, 이후 issue별 순차 버전 |

## 2. 리스크 상세

### RISK-01: Java 24 라이브러리 호환성

- **카테고리**: 기술
- **설명**: Java 24는 2025-03 GA 직후의 최신 non-LTS 릴리스이다. Spring Boot 3.x 자체는 Java 24를 지원하지만, 서드파티 라이브러리(특히 Springdoc OpenAPI, jjwt, Testcontainers, ArchUnit 등)가 Java 24의 sealed classes, pattern matching 등 새 바이트코드 기능과 호환되지 않을 가능성이 있다. 또한 `--add-opens` 플래그가 필요한 리플렉션 기반 라이브러리에서 런타임 경고 또는 오류가 발생할 수 있다.
- **영향**: 3 (빌드 실패 또는 런타임 오류로 Sprint 0 일정 지연, 최악의 경우 JDK 다운그레이드 필요)
- **가능성**: 3 (non-LTS 버전 특성상 일부 라이브러리 미지원은 빈번)
- **현재 상태**: 식별
- **트리거 신호**: Sprint 0 의존성 resolution 시 `./gradlew dependencies` 실패, 컴파일 경고 중 `--add-opens` 관련 메시지 출현, 특정 라이브러리의 GitHub Issues에 Java 24 호환성 이슈 등록
- **완화 전략**:
  1. Sprint 0 첫 작업으로 전체 의존성 빌드 + 단위 테스트 실행하여 호환성 조기 검증
  2. `build.gradle`의 `toolchain` 블록에 Java 21 LTS fallback을 주석 포함하여 준비
  3. 호환 불가 라이브러리 발견 시 즉시 Java 21로 전환 (Spring Boot 3.x는 Java 21 완벽 지원)
  4. JVM args에 `--add-opens` 플래그를 미리 설정하여 리플렉션 경고 방지
- **대응 이슈**: Sprint 0 - 프로젝트 스캐폴딩 및 의존성 검증

### RISK-02: PostgreSQL 로컬 환경 설치 부담

- **카테고리**: 운영
- **설명**: 본 프로젝트는 로컬 전용 실행 방침(클라우드 배포 없음)을 채택하고 있어 모든 개발자가 PostgreSQL을 로컬 머신에 직접 설치하거나 Docker를 통해 실행해야 한다. Windows/macOS/Linux 각 환경에서 PostgreSQL 설치 경로, 포트 충돌(5432), 인코딩 설정(UTF-8) 등이 다를 수 있으며, Docker Desktop 미설치 환경에서는 추가 설정 부담이 발생한다.
- **영향**: 2 (개발 환경 구성 지연, 최초 1회성 부담)
- **가능성**: 3 (다양한 OS 환경에서 DB 설정 차이는 흔한 문제)
- **현재 상태**: 식별
- **트리거 신호**: 개발자 온보딩 시 `./gradlew bootRun` 실행에서 DB 연결 실패, Docker Compose 실행 시 포트 충돌 에러, Flyway 마이그레이션 시 인코딩 관련 오류
- **완화 전략**:
  1. `docker-compose.yml`에 PostgreSQL 16 서비스를 포함하여 `docker compose up -d` 한 줄로 DB 기동 가능하게 제공
  2. `LOCAL.md` 6장에 OS별(Windows/macOS/Linux) PostgreSQL 설치 + Docker 대안 가이드 명시
  3. `.env.dev.example`에 DB 접속 정보 기본값(localhost:5432, conduit/conduit) 미리 기재
  4. H2 in-memory DB를 test profile에서 사용하여 통합 테스트 시 PostgreSQL 의존성 제거
- **대응 이슈**: Sprint 0 - Docker Compose 및 LOCAL.md 작성

### RISK-03: Hexagonal Architecture 학습 곡선

- **카테고리**: 기술
- **설명**: 본 프로젝트는 Hexagonal Architecture(Ports & Adapters) + DDD 전술 패턴을 채택한다. 일반적인 Layered Architecture(Controller-Service-Repository)와 달리 port 인터페이스, adapter 구현체, 도메인 모델의 분리가 필요하며, 의존 방향(domain -> 외부 절대 금지)을 엄격히 지켜야 한다. 이 패턴에 익숙하지 않은 개발자가 adapter에서 domain 로직을 작성하거나, domain 모듈이 infrastructure에 의존하는 실수를 범할 수 있다.
- **영향**: 3 (구조 위반이 누적되면 리팩토링 비용 급증, 모듈 간 순환 의존 발생 가능)
- **가능성**: 2 (Sprint 0에서 템플릿을 제공하면 첫 구현부터 올바른 패턴 적용 가능)
- **현재 상태**: 식별
- **트리거 신호**: PR 리뷰에서 의존 방향 위반 반복 발견, ArchUnit 테스트 실패, domain 모듈의 import 문에 `infrastructure` 또는 `adapter` 패키지 출현
- **완화 전략**:
  1. `11-coding-conventions`에 의존 방향 규칙을 다이어그램 포함하여 명시 (domain <- application <- adapter, 화살표 방향 = 의존 방향)
  2. Sprint 0에서 User 모듈을 완성된 레퍼런스 구현(port, adapter, domain entity, application service)으로 제공
  3. ArchUnit 의존성 규칙 테스트를 Sprint 0에서 작성하여 CI에서 구조 위반을 자동 차단
  4. 패키지 구조 템플릿: `<module>/domain/`, `<module>/application/port/in/`, `<module>/application/port/out/`, `<module>/adapter/in/web/`, `<module>/adapter/out/persistence/`
- **대응 이슈**: Sprint 0 - 코딩 컨벤션 적용 및 레퍼런스 모듈 구현

### RISK-04: Spring Modulith 미성숙

- **카테고리**: 기술
- **설명**: Spring Modulith는 Spring Boot 3.1(2023-05)부터 GA이지만, 커뮤니티 사용 사례가 JPA/Spring Data 대비 상대적으로 적다. 모듈 경계 검증(`@ApplicationModuleTest`), 이벤트 기반 모듈 간 통신(`ApplicationModuleListener`), 문서화(`Documenter`) 기능을 사용할 예정이나, 복잡한 모듈 의존 그래프에서 예기치 않은 검증 실패나 API 변경이 발생할 수 있다.
- **영향**: 2 (모듈 테스트 실패 시 해당 기능만 Spring Boot native로 대체하면 되므로 전체 일정 영향 제한적)
- **가능성**: 2 (사용 범위를 `@ApplicationModuleTest`로 한정하면 안정 영역 내 활용)
- **현재 상태**: 식별
- **트리거 신호**: `@ApplicationModuleTest` 실행 시 모듈 탐지 실패, Spring Modulith 버전 업 후 API breaking change, 모듈 간 순환 의존 탐지 시 불명확한 에러 메시지
- **완화 전략**:
  1. Spring Modulith 사용 범위를 `@ApplicationModuleTest`(모듈 경계 검증)로 한정
  2. 이벤트 기반 통신은 Spring ApplicationEvent 표준 API 사용 (Modulith 전용 API 회피)
  3. Modulith 기능 장애 발생 시 해당 테스트를 `@SpringBootTest`로 대체하는 fallback 절차 마련
  4. Spring Modulith 버전을 Spring Boot BOM 관리 버전에 고정하여 호환성 보장
- **대응 이슈**: Sprint 0 - 모듈 경계 설정 및 ApplicationModuleTest 작성

### RISK-05: RealWorld Token 인증 스펙 비표준

- **카테고리**: 기술
- **설명**: RealWorld 스펙은 `Authorization: Token jwt.token.here` 형식을 사용한다. 이는 HTTP 표준인 `Authorization: Bearer <token>`과 다르며, Spring Security의 기본 `BearerTokenAuthenticationFilter`를 그대로 사용할 수 없다. 커스텀 `JwtAuthFilter`를 작성하여 `Token` prefix를 파싱하고 JWT를 추출해야 하며, 이 과정에서 Security Filter Chain 순서, 예외 처리(`AuthenticationEntryPoint`), CORS 설정과의 상호작용에서 미묘한 버그가 발생할 수 있다.
- **영향**: 3 (인증 실패는 전체 API의 보호 엔드포인트 접근 불가로 이어짐, F-01~03 및 모든 인증 필요 기능에 영향)
- **가능성**: 2 (구현 패턴이 명확하고 RealWorld 커뮤니티에 레퍼런스가 존재)
- **현재 상태**: 식별
- **트리거 신호**: 로그인 후 발급된 JWT로 프로필 조회(`GET /api/user`)가 401 반환, Postman/Newman 테스트에서 인증 헤더 파싱 실패, CORS preflight(OPTIONS) 요청이 필터에 의해 차단
- **완화 전략**:
  1. Sprint 1 초기에 `JwtAuthFilter`를 `OncePerRequestFilter` 기반으로 구현하여 `Token` prefix 파싱
  2. Security Filter Chain에서 `JwtAuthFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 배치
  3. 통합 테스트(MockMvc + TestRestTemplate)에서 `Token` 헤더 인증 시나리오를 Sprint 1에서 즉시 검증
  4. Optional 엔드포인트(비로그인에서도 접근 가능하나 로그인 시 추가 정보 제공)에 대한 분기 처리 명시
- **대응 이슈**: Sprint 1 - 인증/인가 모듈 구현 (F-01, F-02)

### RISK-06: CDN 외부 의존성

- **카테고리**: 외부 의존
- **설명**: 프론트엔드는 Bootstrap 4 CSS, Ionicons, Google Fonts(Titillium Web, Source Serif Pro 등)를 CDN에서 로드한다. 네트워크 단절, CDN 장애, 또는 오프라인 개발 환경에서 스타일과 아이콘이 깨질 수 있다. 본 프로젝트는 로컬 전용 실행 방침이므로 인터넷 연결이 불안정한 환경에서의 개발 가능성을 고려해야 한다.
- **영향**: 2 (UI 스타일링 깨짐, 기능 자체에는 영향 없음)
- **가능성**: 1 (로컬 개발 환경에서 인터넷 단절은 드문 상황)
- **현재 상태**: 식별
- **트리거 신호**: 브라우저 DevTools Network 탭에서 CDN 리소스 로드 실패(ERR_CONNECTION_REFUSED), 스타일 미적용으로 레이아웃 깨짐, Ionicons 아이콘 미표시
- **완화 전략**:
  1. `index.html`에 CDN 로드 실패 시 로컬 번들로 전환하는 fallback `<link>` 태그 준비
  2. `public/` 디렉토리에 Bootstrap 4 CSS, Ionicons CSS를 로컬 복사본으로 보관
  3. 오프라인 시 시스템 폰트(sans-serif)로 대체되도록 `font-family` fallback stack 설정
  4. 개발 초기에는 CDN 의존을 유지하되, 오프라인 이슈 발생 시 즉시 로컬 번들로 전환
- **대응 이슈**: Sprint 1~3 - 프론트엔드 전반 (필요 시 대응)

### RISK-07: Flyway 마이그레이션 충돌

- **카테고리**: 운영
- **설명**: Flyway는 `V{version}__{description}.sql` 형식의 순차 버전 관리를 사용한다. 여러 개발자가 동시에 서로 다른 Sprint/Issue에서 스키마 변경을 추가할 때, 동일한 버전 번호(예: V2)를 부여하면 마이그레이션 충돌이 발생한다. 또한 이미 적용된 마이그레이션 파일의 체크섬이 변경되면 Flyway가 기동을 거부한다. 본 프로젝트는 5개 Sprint에 걸쳐 점진적으로 테이블을 추가하므로 이 리스크가 현실화될 가능성이 있다.
- **영향**: 3 (마이그레이션 실패 시 애플리케이션 기동 불가, 수동 DB repair 필요)
- **가능성**: 2 (Sprint 0에서 초기 스키마를 한 번에 생성하면 이후 변경 빈도 감소)
- **현재 상태**: 식별
- **트리거 신호**: `./gradlew bootRun` 시 `FlywayValidateException` 발생, `flyway_schema_history` 테이블에 동일 버전 중복 기록, PR 머지 후 다른 브랜치에서 마이그레이션 순서 충돌
- **완화 전략**:
  1. Sprint 0에서 `V1__init_schema.sql` 단일 파일로 전체 초기 스키마(users, articles, tags, comments, favorites, follows) 생성
  2. 이후 스키마 변경은 Issue 번호 기반 버전 부여: `V{sprint}{issue}__{description}.sql` (예: V1001__add_bio_column.sql)
  3. PR 머지 전 `flyway validate` 명령으로 충돌 여부 사전 검증
  4. 충돌 발생 시 `flyway repair` + 버전 번호 재부여 절차를 `LOCAL.md`에 문서화
- **대응 이슈**: Sprint 0 - DB 초기화 및 Flyway 설정, Sprint 1~3 - 스키마 변경 시 적용

## 3. High 리스크 단계적 롤아웃

현재 식별된 7건의 리스크 중 High 등급(영향 x 가능성 >= 12 또는 영향 5)은 없다. 모든 리스크가 Medium(4건) 또는 Low(3건)으로 평가되었다.

Medium 등급 리스크에 대한 단계적 대응 계획은 다음과 같다.

### Sprint 0 집중 완화 (RISK-01, RISK-02, RISK-03, RISK-07)

Sprint 0(프로젝트 부트스트랩)에서 4건의 Medium 리스크를 선제 완화한다.

| 단계 | 대상 RISK | 활동 | 완료 기준 |
|---|---|---|---|
| 1단계: 의존성 검증 | RISK-01 | Java 24 환경에서 전체 `./gradlew build` 성공 확인 | 빌드 + 테스트 GREEN, 경고 0건 |
| 2단계: 인프라 구성 | RISK-02 | Docker Compose + LOCAL.md 작성, 3 profile 부팅 검증 | `docker compose up` 후 DB 연결 성공 |
| 3단계: 구조 템플릿 | RISK-03 | User 모듈 레퍼런스 구현 + ArchUnit 의존 규칙 테스트 | ArchUnit 테스트 GREEN |
| 4단계: 스키마 초기화 | RISK-07 | `V1__init_schema.sql` 작성 + Flyway 마이그레이션 실행 | `flyway validate` 성공 |

### Sprint 1 모니터링 (RISK-05)

Sprint 1에서 인증 모듈 구현 시 RISK-05를 집중 모니터링한다.

| 단계 | 활동 | 완료 기준 |
|---|---|---|
| 1단계: 필터 구현 | `JwtAuthFilter`에서 `Token` prefix 파싱 구현 | 단위 테스트 GREEN |
| 2단계: 통합 검증 | MockMvc로 인증 헤더 시나리오 테스트 | 로그인 -> 토큰 발급 -> 프로필 조회 성공 |
| 3단계: E2E 검증 | Postman Collection으로 RealWorld 스펙 준수 확인 | Auth 엔드포인트 전체 PASS |

### 상시 모니터링 (RISK-04, RISK-06)

Low 등급 리스크 2건은 별도 롤아웃 없이 상시 모니터링하며, 트리거 신호 발생 시 완화 전략을 즉시 적용한다.

| RISK | 모니터링 방법 | 에스컬레이션 조건 |
|---|---|---|
| RISK-04 | Sprint 0 `@ApplicationModuleTest` 실행 결과 확인 | 모듈 테스트 2회 연속 실패 시 `@SpringBootTest`로 전환 |
| RISK-06 | 브라우저 DevTools Network 탭에서 CDN 로드 상태 확인 | CDN 장애 1회 발생 시 로컬 번들 fallback 적용 |
