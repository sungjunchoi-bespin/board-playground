# board-playground — Planning CHANGELOG

> 계획 문서 + 주요 변경 사항의 시간순 기록. 코드 변경은 git log / GitHub Releases 참조.

## Current Status

- **Mode**: sprint
- **Active Sprint**: Sprint 1 종료 (Issue #2~#26 closed) + 후속 polish (Issue #52 in-progress)
- **Last Update**: 2026-05-20

## Unreleased

### 2026-05-20

- **design(frontend) #52** — UI 비주얼 production 폴리싱 진행 중
  - 디자인 토큰 일관화: 146건 hex 하드코딩 → CSS 변수 0건
  - 접근성: aria-label, semantic HTML, `:focus-visible` 전역 룰
  - 반응형: 768px·480px 미디어쿼리 4개 페이지 적용
  - 상태 UI: `LoadingState`/`EmptyState`/`ErrorState` 컴포넌트 추출 + 9개 화면 적용
  - 리팩토링: `ArticlePreview` 공통 컴포넌트 추출 (home/profile 중복 제거)
  - 사전 정리: backend google-java-format 28파일 + frontend lockfile 정리

## Released

### 2026-05-20 — Sprint 1 종료

- #26 chore(backend): 최종 점검 — SLF4J 로깅 + 에러 통일 + README
- #25 docs(backend): Springdoc OpenAPI 문서화 — 19 엔드포인트
- #24 test(frontend): Playwright E2E 골든패스 13 시나리오
- #23 test(backend): 전체 API 통합 테스트 — 18 엔드포인트
- #22 feat(frontend): 즐겨찾기 버튼 (Optimistic Update)
- #21 feat(frontend): 댓글 섹션
- #20 feat(frontend): Profile 페이지
- #19 feat(backend): Favorite/Unfavorite (멱등성)
- #18 feat(backend): Comment CRUD (권한 검증)
- #17 feat(backend): Profile + Follow/Unfollow
- #16 feat(frontend): 홈 피드 (탭/사이드바/페이지네이션)
- #15 feat(frontend): Article 상세
- #14 feat(frontend): Editor
- #13 feat(backend): Article 목록/피드
- #12 feat(backend): Tag 모듈
- #11 feat(backend): Article CRUD
- #10 test(backend): 인증 플로우 통합 테스트
- #9 feat(frontend): Settings 페이지
- #8 feat(frontend): Login/Register
- #7 feat(backend): User 도메인 (4 엔드포인트)
- #6 feat(frontend): App Shell
- #5 chore(backend): Flyway + JPA + Testcontainers
- #4 feat(backend): Spring Security + JWT + CORS
- #3 chore(frontend): React 18 + Vite + TS 스캐폴딩
- #2 chore(backend): Hexagonal + Spring Boot 3 스캐폴딩

### 2026-05-19 — Gate C 통과

- 계획 산출 01~15 모두 작성 완료 (sprint-bootstrap PR #1)
- 25 이슈 + 5 milestones + 19 labels 등록
