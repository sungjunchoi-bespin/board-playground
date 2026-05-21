# code-explainer Plugin

코드를 **3단계 스캐폴딩(L1 Architecture / L2 Modules / L3 Code)**으로 분석하여 주니어 개발자용 학습 문서를 생성하고, **GitHub Discussion + Wiki에 동시 적재**하는 Claude Code 플러그인입니다.

## Prerequisites

- [Claude Code](https://claude.ai/code) CLI 설치
- [GitHub CLI (`gh`)](https://cli.github.com/) 설치 및 인증 (`gh auth login`)
- Discussion이 활성화된 GitHub 저장소
- (선택) Wiki가 활성화되고 첫 페이지가 1개 이상 만들어진 저장소 — Wiki에도 동시 적재하려면 필요. 첫 페이지는 `https://github.com/<owner>/<repo>/wiki`의 "Create the first page"로 한 번만 만들면 됩니다. 없으면 Discussion만 적재됩니다.

## 설치

```bash
# 1. 플러그인 클론
git clone https://github.com/sungjunchoi-bespin/code-explainer-plugin.git

# 2. 설정 파일 수정
cd code-explainer-plugin
vi config.json
```

`config.json`에 자신의 저장소 정보를 입력합니다:

```json
{
  "discussion_repo_owner": "my-org",
  "discussion_repo_name": "my-code-docs",
  "webhook_url": ""
}
```

| 항목 | 설명 | 필수 |
|------|------|------|
| `discussion_repo_owner` | Discussion이 활성화된 GitHub 저장소 소유자 | O |
| `discussion_repo_name` | Discussion이 활성화된 GitHub 저장소 이름 | O |
| `webhook_url` | Google Chat Webhook URL (비어있으면 알림 스킵) | X |

```bash
# 3. 플러그인 로드하여 Claude Code 실행
claude --plugin-dir ./code-explainer-plugin
```

## 사용법

Claude Code 안에서 슬래시 커맨드로 실행합니다:

```
# 전체 저장소 분석
/code-explainer https://github.com/my-org/my-project

# 특정 폴더 분석
/code-explainer src/auth/

# 단일 파일 분석
/code-explainer src/auth/AuthService.java
```

자연어로도 요청 가능합니다:

```
"이 코드 분석해줘"
"src/auth/ 설명해줘"
```

## 동작 방식

요청하면 **중간 확인 없이 끝까지 자동 실행**됩니다.

```
작업 지시 (파일 경로 / Git URL)
    │
    ▼
code-explainer 스킬
    │
    ├─ Phase 0:   설정 확인 (config.json + gh 인증 + Discussion 활성화 + Wiki 초기화)
    ├─ Step 1~5:  코드 스캔 → L1 → L2(디렉토리별) → L3(파일별) 문서 생성
    ├─ Phase 3:   Label 자동 생성 + General 카테고리에 Discussion 적재
    ├─ Phase 3.5: Wiki repo clone + L1/L2/L3 페이지 작성 + Home/_Sidebar 생성 + push
    │             (Wiki 초기화 안 됐으면 이 단계만 스킵)
    ├─ Phase 4:   PR 코멘트에 Discussion + Wiki 링크 추가 (PR이 있을 때만)
    └─ Phase 5:   Google Chat 알림 (webhook_url이 있을 때만)
```

## Discussion / Wiki 구조

소스 코드의 디렉토리 구조를 그대로 반영합니다:

| 레벨 | 단위 | 설명 |
|------|------|------|
| L1 · Architecture | 프로젝트 1개 | 전체 아키텍처, 데이터 흐름, 기술 선택 |
| L2 · Modules | 디렉토리마다 1개 | 패키지 책임, 파일 목록, 의존 관계 |
| L3 · Code | 소스 파일마다 1개 | **함수/메서드 블록마다** 코드+설명 + 주니어 팁 |

### Discussion 측

모든 Discussion은 **General** 카테고리에 생성되고, **Label**로 분류됩니다.

### Wiki 측

Wiki는 평면 구조이므로 파일명 prefix로 분류합니다:

| 파일명 | 용도 |
|--------|------|
| `L1-<프로젝트>.md` | L1 페이지 1개 |
| `L2-<패키지-경로>.md` | 디렉토리마다 1개 |
| `L3-<패키지-경로>-<파일명>.md` | 소스 파일마다 1개 |
| `Home.md` | 진입 페이지 (L1/L2/L3 전체 인덱스) |
| `_Sidebar.md` | 좌측 네비게이션 (모든 페이지에 자동 표시) |

버전 이력은 Discussion은 코멘트 백업으로, Wiki는 git history로 자동 누적됩니다.

Discussion Label 색상:

| Label | 용도 | 색상 |
|-------|------|------|
| `L1` | 아키텍처 레벨 | 빨강 `d73a4a` |
| `L2` | 모듈 레벨 | 파랑 `0075ca` |
| `L3` | 코드 레벨 | 초록 `008672` |
| 프로젝트명 | 프로젝트 구분 | 노랑 `e4e669` |

GitHub UI에서 Label로 필터링:

```
label:L2 label:my-project     → 모듈(디렉토리) 문서만
label:L3 label:my-project     → 소스 파일 문서만
```

## 버전 관리

- **Discussion**: 업데이트 시 이전 본문은 Comment로 보관되어 이력이 누적됩니다.
- **Wiki**: git history로 자동 누적되며 페이지 우측 상단 "Revisions" 탭에서 과거 버전 비교 가능.

```
> 📌 v2 · 2026-05-08 11:32:04
```

## 전체 프로젝트 문서

Discussion 구조, 각 레벨별 필수 포함 내용, 프로젝트 구조 등 자세한 내용은 [메인 README](https://github.com/sungjunchoi-bespin/code-explainer#readme)를 참고하세요.

## License

MIT
