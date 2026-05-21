---
name: code-explainer
description: >
  코드를 주니어 개발자가 이해할 수 있도록 3단계 스캐폴딩 설명을 생성하고, GitHub Discussion과 Wiki에 동시에 적재합니다.
  "이 코드 설명해줘", "주니어가 이해할 수 있게 정리해줘" 등의 요청 시 이 스킬을 사용하세요.
argument-hint: <폴더 경로 | 파일 경로 | Git 주소>
allowed-tools: [Read, Glob, Grep, Bash, Agent, Write, Edit]
---

# /code-explainer

코드를 **3단계 스캐폴딩**으로 분해하여 주니어 개발자가 학습할 수 있는 설명 문서를 생성합니다.
**Discussion 구조는 프로젝트의 디렉토리 구조를 그대로 반영합니다.**
**레벨과 프로젝트 구분은 Discussion에서는 Label, Wiki에서는 페이지 prefix로 처리하여 수동 세팅이 필요 없습니다.**

생성된 문서는 두 개의 sink에 동시 적재됩니다:

1. **GitHub Discussions** — 라벨 기반 분류, 코멘트로 버전 이력 누적
2. **GitHub Wiki** — `Home.md` + `_Sidebar.md`로 탐색, git history로 버전 이력 자동 누적

**모든 출력은 한국어로 작성합니다.**

## Usage

```
/code-explainer <폴더 경로 | 파일 경로 | Git 주소>
```

---

## Phase 0: 설정 확인 (분석 전 필수)

분석을 시작하기 **전에** 아래를 순서대로 확인합니다. 하나라도 실패하면 분석을 시작하지 않습니다.

### 1. config.json 확인

!cat "${CLAUDE_SKILL_DIR}/../../config.json"

위 `config.json`을 확인합니다:

- **`discussion_repo_owner` 또는 `discussion_repo_name`이 비어있으면**:
  1. 사용자에게 Discussion을 적재할 GitHub 저장소 **owner**와 **이름**을 입력받습니다 (필수)
  2. Google Chat Webhook URL을 입력받습니다 (선택 — 비어있으면 알림 스킵)
  3. 입력받은 값을 `${CLAUDE_SKILL_DIR}/../../config.json`에 저장합니다
- 값이 이미 채워져 있으면 그대로 사용합니다

**필수값이 비어있으면** 아래 메시지를 반환하고 파이프라인을 중단합니다:

```
⚠️ config.json 설정이 필요합니다.
플러그인 디렉토리의 config.json에서 아래 값을 설정해주세요:
- discussion_repo_owner: GitHub 저장소 소유자
- discussion_repo_name: GitHub 저장소 이름
```

### 2. GitHub CLI 인증 확인

`gh auth status`를 실행합니다. 인증이 안 되어있으면 `gh auth login`을 안내하고 **중단**합니다.

### 3. Discussion 활성화 확인

대상 저장소에 Discussion이 활성화되어 있는지 확인합니다.
활성화되어 있지 않으면 Settings → Features → Discussions 활성화를 안내하고 **중단**합니다.

### 4. Wiki 활성화 + 초기화 확인 (선택적 — 실패 시 Wiki만 스킵)

Wiki는 **Discussion과 달리 GraphQL API가 없어** `<repo>.wiki.git`을 클론·푸시하는 방식으로 적재합니다.
따라서 사전 조건이 두 단계로 나뉩니다:

1. **Wiki 기능 활성화 여부** (`has_wiki`):
   ```bash
   gh api repos/${OWNER}/${REPO} --jq '.has_wiki'
   ```
   `false`면 Settings → Features → Wikis 체크를 안내하고 **Wiki Phase만 스킵** (Discussion은 계속 진행).

2. **Wiki repo 초기화 여부** (`<repo>.wiki.git` 존재):
   ```bash
   git ls-remote https://github.com/${OWNER}/${REPO}.wiki.git 2>&1 | head -1
   ```
   `Repository not found`가 나오면 wiki repo가 아직 생성되지 않은 상태입니다.
   **GitHub Wiki는 첫 페이지를 UI에서 한 번 수동으로 만들어야 wiki repo가 초기화**됩니다.
   초기화 안 됐으면 아래 안내를 출력하고 **Wiki Phase만 스킵** (Discussion은 계속 진행):

   ```
   ℹ️ Wiki repo가 아직 초기화되지 않았습니다.
   https://github.com/${OWNER}/${REPO}/wiki 에서 "Create the first page" 버튼으로
   아무 페이지 1개를 만든 뒤 다시 실행하면 Wiki에도 적재됩니다.
   이번 실행은 Discussion만 적재합니다.
   ```

> Discussion 사전 조건이 통과되면 코드 분석을 시작합니다. Wiki 사전 조건은 실패해도 Discussion 적재는 계속되며, Phase 3.5만 스킵됩니다.

---

## 핵심 원칙

### 1. 디렉토리 = Discussion

- **L1** (1개) = 프로젝트 전체 아키텍처
- **L2** (디렉토리마다 1개) = 소스 파일이 존재하는 모든 패키지/디렉토리
- **L3** (소스 파일마다 1개) = 모든 소스 파일 (DTO, Router, 유틸 포함, 예외 없음)

### 2. Label = 자동 분류 (수동 세팅 없음)

모든 Discussion은 **General** 카테고리(기본 제공)에 생성하고, **Label**로 레벨과 프로젝트를 구분합니다.
Category 수동 생성이 필요 없어 어떤 저장소에서든 Discussion만 활성화되어 있으면 바로 작동합니다.

| Label | 용도 | 예시 |
|-------|------|------|
| `L1` | 아키텍처 레벨 | `L1` |
| `L2` | 모듈(디렉토리) 레벨 | `L2` |
| `L3` | 코드(파일) 레벨 | `L3` |
| 프로젝트명 | 프로젝트 구분 | `t2s-admin-backend` |

**GitHub UI에서 필터링:**
```
label:L2 label:t2s-admin-backend     → T2S의 모듈만
label:L3 label:t2s-admin-backend     → T2S의 소스 파일만
label:claude-code                     → Claude Code 전체
```

### 예시: Java Modulith 프로젝트

```
소스 디렉토리 구조                          → Discussion + Labels
─────────────────────────────────────────────────────────────────
com/cognet9/modulith/                       → 🏗️ 전체 구조 [L1] [t2s-admin-backend]
├── core/config/security/                   → 📦 core.config.security [L2] [t2s-admin-backend]
│   ├── SecurityConfig.java                 → 🔬 core...security/SecurityConfig.java [L3] [t2s-admin-backend]
│   ├── AuthContext.java                    → 🔬 core...security/AuthContext.java [L3] [t2s-admin-backend]
│   └── jwt/                                → 📦 core.config.security.jwt [L2] [t2s-admin-backend]
│       ├── JwtTokenProvider.java           → 🔬 core...jwt/JwtTokenProvider.java [L3] [t2s-admin-backend]
│       └── JwtAuthenticationFilter.java    → 🔬 core...jwt/JwtAuthenticationFilter.java [L3] [t2s-admin-backend]
├── modules/account/
│   ├── adapter/in/web/                     → 📦 modules.account.adapter.in.web [L2] [t2s-admin-backend]
│   │   ├── AccountHandler.java             → 🔬 ...web/AccountHandler.java [L3] [t2s-admin-backend]
│   │   └── AccountRouter.java              → 🔬 ...web/AccountRouter.java [L3] [t2s-admin-backend]
│   ├── application/service/                → 📦 modules.account.application.service [L2] [t2s-admin-backend]
│   │   └── AccountService.java             → 🔬 ...service/AccountService.java [L3] [t2s-admin-backend]
│   └── model/dto/                          → 📦 modules.account.model.dto [L2] [t2s-admin-backend]
│       ├── AccountDto.java                 → 🔬 ...dto/AccountDto.java [L3] [t2s-admin-backend]
│       └── CreateAccountRequest.java       → 🔬 ...dto/CreateAccountRequest.java [L3] [t2s-admin-backend]
```

---

## 설정 정보

이 스킬은 플러그인 `config.json`에서 설정을 읽습니다.

| 항목 | config.json 키 | 설명 |
|------|----------------|------|
| Discussion 대상 저장소 owner | `discussion_repo_owner` | GitHub 저장소 소유자 (예: `my-org`) |
| Discussion 대상 저장소 name | `discussion_repo_name` | GitHub 저장소 이름 (예: `my-code-docs`) |
| Google Chat Webhook URL | `webhook_url` | 비어있으면 Phase 5 스킵 |

> **중요**: 업로드 스크립트 생성 시, config.json 값을 **Python 변수에 직접 삽입**합니다.
> 런타임에 config.json을 읽지 마세요 — 실행 경로에 따라 파일을 찾지 못할 수 있습니다.

```python
# ✅ 올바른 방법: Phase 0에서 확인된 실제 값을 직접 삽입
OWNER = "my-org"
REPO = "my-code-docs"
WEBHOOK_URL = "https://chat.googleapis.com/..."  # 비어있으면 ""

# ❌ 잘못된 방법: 런타임에 config.json 읽기 (경로 문제 발생)
# with open("config.json") as f: config = json.load(f)
```

---

## 코드 리뷰 기준 매핑

| 코드 리뷰 필수 요소 | 주로 다루는 레벨 |
|-------------------|----------------|
| #3 작고 집중된 단위의 PR | Step 1 스캔 (범위 경고) |
| #4 명확한 PR 설명 (What·Why) | L1 |
| #6 설계 검토 | L1, L2 |
| #8 복잡성 검토 / #9 네이밍 검토 | L2 |
| #7 기능성 / #10 오류 처리 / #11 보안 | L3 |
| #12·13 테스트 포함·품질 | L2, L3 |
| #14 코딩 표준·스타일 / #15 주석·문서화 | L3 |

---

## 분석 및 생성 단계

### Step 1 · 코드 스캔

1. **프로젝트명 추출**: Git URL 또는 디렉토리명에서 → Label 이름 결정
2. **디렉토리 트리 추출** — 소스 파일이 있는 모든 디렉토리를 나열 → L2 목록 확정
3. **소스 파일 전수 목록** — 모든 소스 파일을 나열 → L3 목록 확정 (예외 없음)
4. **도메인 결정** — 프로젝트가 무엇인가 → L1 Section 이름

### Step 2 · 코드 읽기

모든 소스 파일을 읽어서 분석합니다. 파일 수가 많으면 병렬로 읽습니다.

### Step 3 · L1 생성 — 전체 지도 (1개)

### Step 4 · L2 생성 — 디렉토리마다 1개

소스 파일이 존재하는 **모든 디렉토리(패키지)**마다 L2 Discussion을 생성합니다.
Discussion 제목에 패키지 경로를 포함하여 프로젝트 구조가 그대로 보이도록 합니다.

### Step 5 · L3 생성 — 소스 파일마다 1개

**모든 소스 파일**에 대해 L3 Discussion을 생성합니다.
DTO, Router, 보일러플레이트, 단순 유틸 등을 포함하여 **예외 없이 전부** 생성합니다.
Discussion 제목에 패키지 경로와 파일명을 포함합니다.

---

## 출력 템플릿

### L1 · Architecture

```markdown
## 🏗️ [프로젝트명] 전체 구조

> Section: [도메인] · Level: L1 · Architecture
> 리뷰 기준: #4 PR 설명, #6 설계 검토

**이 프로젝트가 하는 일**
- What: [한 문장]
- Why: [한 문장]

**전체 흐름**
[Client] → [Filter] → [Handler/Router] → [Service] → [Repository] → [DB]

**디렉토리 구조**
[프로젝트의 실제 디렉토리 트리 — 이것이 L2 Discussion 목록이 됨]

**📖 읽기 순서 가이드**
> 이 프로젝트를 처음 읽는다면 아래 순서를 추천합니다.
1. [첫 번째로 읽을 파일/패키지] — [이유]
2. [두 번째] — [이유]
3. ...

**기술 선택**
| 기술 | 선택 이유 |
|------|-----------|

**📝 용어 사전 (Glossary)**
| 용어 | 설명 |
|------|------|
| [용어] | [주니어가 이해할 수 있는 한 줄 설명] |

**⚙️ 환경/설정 개요**
| 항목 | 값 / 설명 |
|------|-----------|
| [JDK/Node/Python 등] | [버전 및 필요 사항] |
| [DB] | [종류 및 설정] |
| [빌드 도구] | [Gradle/Maven/npm 등] |
| [주요 설정 파일] | [application.yml 등 핵심 설정] |

**📚 주니어 선수 지식** (해당 시)
```

### L2 · Modules (디렉토리)

```markdown
## 📦 [패키지 경로]

> Section: [도메인] · Level: L2 · Modules
> 📂 경로: `src/main/java/com/example/[패키지 경로]`
> 리뷰 기준: #6 설계 검토, #8 복잡성, #9 네이밍

**L1에서의 위치**: [전체 흐름 중 이 패키지가 담당하는 위치]

**한 줄 요약**: [이 디렉토리의 책임]

**포함된 파일**
| 파일명 | 역할 |
|--------|------|
| [FileName.java] | [한 줄] |

**🔄 파일 간 호출 흐름**
```
[파일A] → [파일B] → [파일C]
  설명: [흐름을 한 줄로 요약]
```

**🧩 디자인 패턴 설명**
| 패턴 | 적용 위치 | 주니어 설명 |
|------|-----------|------------|
| [패턴명] | [파일/클래스] | [왜 이 패턴을 썼는지, 안 쓰면 어떻게 되는지] |

**의존 관계**: [이 패키지가 사용하는 다른 패키지]
```

### L3 · Code (소스 파일)

**L3는 소스 파일 내의 함수/메서드 블록마다 개별 코드+설명 섹션을 생성합니다.**
파일 전체를 하나의 코드 블록으로 퉁치지 않고, 함수 단위로 분리하여 각각 설명합니다.

```markdown
## 🔬 [패키지경로/FileName.java]

> Section: [도메인] · Level: L3 · Code
> 📂 경로: `src/main/java/com/example/[전체 경로]`
> 리뷰 기준: [적용된 리뷰 기준]

**📋 한 줄 요약**: [이 파일이 하는 일을 한 문장으로]

---

### 📍 이 코드의 위치

**L1 아키텍처에서**: [전체 흐름 중 어디]
**L2 패키지에서**: [어느 패키지의 어느 역할]

---

### 🔗 연관 파일 (See Also)

| 파일 | 관계 | 설명 |
|------|------|------|
| [파일명] | [호출/구현/사용 등] | [어떤 관계인지 한 줄] |

---

### 0. 클래스 구조 (import, 필드, 어노테이션)

\`\`\`[언어]
[import문, 클래스 선언, 필드, 의존성 주입 등]
\`\`\`

**설명**

| 줄 | 코드 | 의도 | 설명 | 리뷰 기준 |
|----|------|------|------|----------|

---

### 1. [함수명/메서드명]

\`\`\`[언어]
[해당 함수의 코드]
\`\`\`

**설명**

| 줄 | 코드 | 의도 | 설명 | 리뷰 기준 |
|----|------|------|------|----------|
| [줄번호] | `[코드]` | [왜 — 한 문장] | [쉬운 설명 1~2줄] | #[번호] [기준명] |

---

### 2. [함수명/메서드명]

\`\`\`[언어]
[해당 함수의 코드]
\`\`\`

**설명**

| 줄 | 코드 | 의도 | 설명 | 리뷰 기준 |
|----|------|------|------|----------|
| [줄번호] | `[코드]` | [왜 — 한 문장] | [쉬운 설명 1~2줄] | #[번호] [기준명] |

---

(함수 개수만큼 반복)

---

### 💡 주니어 팁
[이 파일/패턴을 처음 보는 사람이 알아야 할 것]

### 🔒 보안·오류 처리 (해당 시)
### ❌ 흔한 실수 (해당 시)
```

> **핵심**:
> - `📍 이 코드의 위치` 절대 생략 금지
> - L3 제목에 패키지 경로 포함 필수
> - **함수/메서드 블록마다** 개별 코드+설명 섹션 생성 (파일 전체를 하나로 묶지 않음)
> - 클래스 필드 선언, import, 어노테이션 등 함수 외부 코드는 "클래스 구조" 섹션으로 맨 앞에 배치
> - L1에는 반드시 📖 읽기 순서, 📝 용어 사전, ⚙️ 환경/설정 개요 포함
> - L2에는 반드시 🔄 파일 간 호출 흐름, 🧩 디자인 패턴 설명 포함
> - L3에는 반드시 📋 한 줄 요약 (헤더 바로 뒤), 🔗 연관 파일 (📍 위치 뒤) 포함

---

## Discussion 제목 및 Label 규칙

| 레벨 | 제목 형식 | Labels |
|------|-----------|--------|
| L1 | `🏗️ [프로젝트명] 전체 구조` | `L1`, `[프로젝트명]` |
| L2 | `📦 [패키지 경로]` | `L2`, `[프로젝트명]` |
| L3 | `🔬 [패키지경로/파일명]` | `L3`, `[프로젝트명]` |

---

## Phase 3: GitHub Discussion 적재

**반드시 Python + `gh api graphql --input -` 방식을 사용합니다.**

### Step 1 · 설정값 삽입 + Repository ID 조회

```python
import json, subprocess

# config.json 값을 직접 삽입 (런타임 파일 읽기 대신)
OWNER = "실제-owner"    # ← config.json의 discussion_repo_owner
REPO = "실제-repo"      # ← config.json의 discussion_repo_name
WEBHOOK_URL = ""        # ← config.json의 webhook_url (비어있으면 알림 스킵)

# Repository ID 동적 조회
repo_id_query = json.dumps({
    "query": f'{{ repository(owner: "{OWNER}", name: "{REPO}") {{ id }} }}'
})
result = subprocess.run(
    ["gh", "api", "graphql", "--input", "-"],
    input=repo_id_query, capture_output=True, text=True
)
REPO_ID = json.loads(result.stdout)["data"]["repository"]["id"]
```

### Step 2 · General 카테고리 ID 조회

```python
# General 카테고리 ID를 동적으로 조회
query = json.dumps({
    "query": f'{{ repository(owner: "{OWNER}", name: "{REPO}") {{ discussionCategories(first: 20) {{ nodes {{ id name }} }} }} }}'
})
# "General" 카테고리의 ID를 추출
```

### Step 3 · Label 생성

프로젝트명과 L1/L2/L3 label이 없으면 자동 생성합니다.

```python
# Label 생성 (없을 때만)
create_label_query = r'mutation($repoId: ID!, $name: String!, $color: String!) { createLabel(input: { repositoryId: $repoId, name: $name, color: $color }) { label { id } } }'

# Label 색상 규칙
# L1: "d73a4a" (빨강)
# L2: "0075ca" (파랑)
# L3: "008672" (초록)
# 프로젝트명: "e4e669" (노랑)
```

### Step 4 · Discussion 생성 + Label 부착

```python
import json, subprocess

# 1. Discussion 생성 (General 카테고리)
create_query = r'mutation($title: String!, $body: String!, $repoId: ID!, $catId: ID!) { createDiscussion(input: { repositoryId: $repoId, categoryId: $catId, title: $title, body: $body }) { discussion { id url } } }'

# 2. Label 부착
label_query = r'mutation($labelableId: ID!, $labelIds: [ID!]!) { addLabelsToLabelable(input: { labelableId: $labelableId, labelIds: $labelIds }) { labelable { labels(first: 5) { nodes { name } } } } }'
```

### 대상 저장소 정보

대상 저장소는 `config.json`의 `discussion_repo_owner`와 `discussion_repo_name`에서 결정됩니다.
Repository ID는 실행 시 GraphQL로 동적 조회합니다.

> General 카테고리 ID와 Label ID는 실행 시 동적으로 조회합니다.

### 적재 순서

1. config.json에서 OWNER, REPO 읽기
2. Repository ID 동적 조회
3. General 카테고리 ID 조회
4. Label 생성 (L1, L2, L3, 프로젝트명) — 없을 때만
5. Label ID 조회
6. L1 Discussion 생성 + Label 부착
7. L2 Discussion 디렉토리 순으로 생성 + Label 부착
8. L3 Discussion 디렉토리 순 > 파일명 순으로 생성 + Label 부착

### 기존 Discussion 처리

같은 제목의 Discussion이 이미 있으면:
1. 기존 본문을 Comment로 보관 (이력 누적)
2. Discussion 본문을 최신 내용으로 교체 (`updateDiscussion` mutation)

---

## Phase 3.5: GitHub Wiki 적재

**Discussion 적재가 끝난 직후 동일한 L1/L2/L3 본문을 Wiki에도 push합니다.**
Phase 0.4의 Wiki 사전 조건이 실패했으면 이 Phase 전체를 스킵합니다.

### 핵심 원칙

| 항목 | Discussion | Wiki |
|------|------------|------|
| 단위 | Discussion 1개 = 페이지 1개 | 마크다운 파일 1개 = 페이지 1개 |
| 분류 | `L1`/`L2`/`L3` + 프로젝트명 Label | 파일명 prefix (`L1-` / `L2-` / `L3-`) |
| 탐색 | 라벨 필터링 | `_Sidebar.md` 좌측 네비게이션 |
| 버전 | 코멘트로 이력 백업 | git history 자동 누적 |

### 페이지 명명 규칙

Wiki는 **평면 파일 구조**이므로 디렉토리 경로는 하이픈으로 평탄화하여 prefix로 식별합니다.

| 레벨 | 파일명 형식 | 예시 |
|------|-------------|------|
| L1 | `L1-<프로젝트명>.md` | `L1-board-playground.md` |
| L2 | `L2-<패키지경로-하이픈>.md` | `L2-backend-src-auth.md` |
| L3 | `L3-<패키지경로-하이픈>-<파일명>.md` | `L3-backend-src-auth-AuthService.md` |
| 진입 | `Home.md` | (고정) |
| 네비 | `_Sidebar.md` | (고정, 좌측 네비 자동 렌더링) |

> 슬래시 `/`도 wiki에서 페이지 prefix로 동작하지만 URL 인코딩 케이스가 까다로워 하이픈을 권장합니다.
> 파일명에 `<`, `>`, `:`, `"`, `\`, `|`, `?`, `*` 가 있으면 `_`로 치환합니다.

### Step 1 · Wiki repo 클론

`gh auth`가 발급한 토큰을 통해 인증된 URL로 클론합니다.

```python
import os, subprocess, tempfile, shutil

OWNER = "실제-owner"     # ← config.json의 discussion_repo_owner 동일
REPO = "실제-repo"        # ← config.json의 discussion_repo_name 동일

# gh CLI가 git에 자격 증명을 위임하도록 helper 설정 (한 번만)
subprocess.run(["gh", "auth", "setup-git"], check=True)

wiki_dir = tempfile.mkdtemp(prefix="code-explainer-wiki-")
wiki_url = f"https://github.com/{OWNER}/{REPO}.wiki.git"
subprocess.run(["git", "clone", wiki_url, wiki_dir], check=True)
```

> 클론이 `Repository not found`로 실패하면 Phase 0.4에서 이미 걸러져 있어야 합니다.
> 그래도 런타임에 실패하면 사용자에게 안내하고 Phase 3.5만 스킵합니다.

### Step 2 · 페이지 파일 작성

생성된 L1/L2/L3 본문을 각각 위 명명 규칙에 따라 `wiki_dir`에 저장합니다.

> **헤더 정책 (중복 H1 회피)**: 본문 원본이 `## 🏗️ ...` 같은 H2로 시작한다면 그 줄을 제거하고 wiki 페이지 첫 줄에 `# {title}` H1을 *단 한 번만* 넣습니다. 두 헤더가 모두 살아 있으면 GitHub Wiki UI에서 같은 제목이 두 번 노출됩니다.

```python
def slugify_path(path: str) -> str:
    """디렉토리 경로/파일명을 wiki 안전 파일명으로 변환"""
    import re
    s = path.replace("/", "-").replace("\\", "-").replace(".", "-")
    s = re.sub(r'[<>:"|?*]', "_", s)
    return s.strip("-")

def write_page(filename: str, title: str, body: str):
    # 원본 첫 줄이 `## ...` H2면 제거 — H1으로 통일
    lines = body.splitlines()
    if lines and lines[0].startswith("## "):
        body = "\n".join(lines[1:]).lstrip("\n")
    full = f"# {title}\n\n{body}"
    path = os.path.join(wiki_dir, filename)
    with open(path, "w", encoding="utf-8") as f:
        f.write(full)

# L1 (1개)
write_page(f"L1-{PROJECT_NAME}.md", l1_title, l1_body)

# L2 (디렉토리마다 1개)
for pkg_path, body in l2_pages.items():
    write_page(f"L2-{slugify_path(pkg_path)}.md", f"📦 {pkg_path}", body)

# L3 (소스 파일마다 1개)
for src_path, body in l3_pages.items():
    write_page(f"L3-{slugify_path(src_path)}.md", f"🔬 {src_path}", body)
```

### Step 3 · Home.md & _Sidebar.md 생성

평면 구조에서도 탐색 가능하도록 진입 페이지와 좌측 네비를 동시에 만듭니다.

> **링크 문법 정책 (BLOCK)**: Gollum의 `[[Page|Display]]` 문법은 GitHub Wiki 사이드바에서 종종 렌더에 실패합니다 (히스토리상 사용자 보고됨). 따라서 **표준 markdown link `[Display](Page)` 형식을 사용**합니다. `Page` 부분은 wiki 파일명 (확장자 없음, 같은 wiki 내 상대 경로).

```python
def link(display: str, page: str) -> str:
    """표준 markdown wiki link — Gollum [[X|Y]] 대신 사용 (BLOCK)."""
    return f"[{display}]({page})"

# Home.md — 진입 페이지 (직접 작성 — write_page 안 거침: 자체 H1 제어)
home_lines = [
    f"# {PROJECT_NAME}\n\n",
    "> code-explainer 자동 생성 학습 문서. 좌측 사이드바 또는 아래 목록에서 L1/L2/L3을 탐색하세요.\n\n",
    "## 🏗️ L1 · Architecture\n\n",
    f"- {link(l1_title.replace('🏗️ ', ''), f'L1-{PROJECT_NAME}')}\n",
    "\n## 📦 L2 · Modules\n\n",
]
for pkg_path in sorted(l2_pages.keys()):
    home_lines.append(f"- {link(pkg_path, f'L2-{slugify_path(pkg_path)}')}\n")

home_lines.append("\n## 🔬 L3 · Code\n\n")
for src_path in sorted(l3_pages.keys()):
    home_lines.append(f"- {link(src_path, f'L3-{slugify_path(src_path)}')}\n")

with open(os.path.join(wiki_dir, "Home.md"), "w", encoding="utf-8") as f:
    f.write("".join(home_lines))

# _Sidebar.md — 좌측 네비 (모든 페이지에 자동 표시)
sidebar_lines = [
    "**🏗️ Architecture**\n\n",
    f"- {link(l1_title.replace('🏗️ ', ''), f'L1-{PROJECT_NAME}')}\n",
    "\n**📦 Modules (L2)**\n\n",
]
for pkg_path in sorted(l2_pages.keys()):
    sidebar_lines.append(f"- {link(pkg_path, f'L2-{slugify_path(pkg_path)}')}\n")

sidebar_lines.append("\n**🔬 Code (L3)**\n\n")
for src_path in sorted(l3_pages.keys()):
    label = src_path.split("/")[-1]  # 파일명만 표시
    sidebar_lines.append(f"- {link(label, f'L3-{slugify_path(src_path)}')}\n")

with open(os.path.join(wiki_dir, "_Sidebar.md"), "w", encoding="utf-8") as f:
    f.write("".join(sidebar_lines))
```

> **링크 검증**: push 직후 `curl -sI {wiki_base}/L1-{PROJECT_NAME}` 등으로 직접 URL이 200을 반환하는지 확인. 페이지 본문은 정상이지만 사이드바 링크가 깨지는 케이스(Gollum 문법 실패)를 미리 감지.

### Step 4 · 커밋 & 푸시

```python
subprocess.run(["git", "-C", wiki_dir, "add", "-A"], check=True)

# 변경 사항이 있을 때만 커밋
status = subprocess.run(
    ["git", "-C", wiki_dir, "status", "--porcelain"],
    capture_output=True, text=True, check=True
).stdout.strip()

if status:
    from datetime import datetime
    msg = f"code-explainer: update {PROJECT_NAME} ({datetime.now().strftime('%Y-%m-%d %H:%M:%S')})"
    subprocess.run(["git", "-C", wiki_dir, "commit", "-m", msg], check=True)
    subprocess.run(["git", "-C", wiki_dir, "push"], check=True)
    print(f"✅ Wiki 업데이트 완료: https://github.com/{OWNER}/{REPO}/wiki")
else:
    print("ℹ️ Wiki 변경 사항 없음 — 커밋 생략")

# 임시 디렉토리 정리
shutil.rmtree(wiki_dir, ignore_errors=True)
```

### 기존 Wiki 페이지 처리

Discussion과 달리 별도의 "이전 본문 백업" 절차가 **필요 없습니다** — wiki repo는 git이므로 새 커밋이 자동으로 history에 누적되며, GitHub UI의 "Revisions" 탭에서 모든 과거 버전을 확인할 수 있습니다.

### 적재 순서

1. `gh auth setup-git` 1회 실행 (idempotent)
2. wiki repo를 임시 디렉토리에 clone
3. L1 / L2 / L3 페이지 파일 작성 (L1 → L2 → L3 순)
4. `Home.md`, `_Sidebar.md` 생성
5. `git add -A` → 변경 있을 때만 `commit` + `push`
6. 임시 디렉토리 삭제

### 실패 처리

- 인증 실패 (403) → `gh auth refresh -s repo` 안내 후 Wiki만 스킵
- clone 실패 (404) → Phase 0.4 안내 재출력 후 Wiki만 스킵
- push 실패 (non-fast-forward) → `git pull --rebase` 후 재푸시 1회 시도, 그래도 실패하면 Wiki만 스킵

> **Discussion 적재는 절대 영향받지 않습니다.** Wiki 실패는 항상 soft-fail.

---

## Phase 4: PR 업데이트

PR이 있는 경우 Discussion 링크와 Wiki Home 링크를 PR 코멘트로 추가합니다.
Wiki Phase가 스킵됐다면 Discussion 링크만 포함합니다.

---

## Phase 5: Google Chat 알림

config.json의 `webhook_url` 값을 사용합니다. **값이 비어있으면 이 Phase를 스킵합니다.**

```python
WEBHOOK_URL = ""  # ← config.json의 webhook_url

if WEBHOOK_URL:
    # Google Chat 알림 전송
    import urllib.request
    wiki_line = f"\n📚 Wiki: https://github.com/{OWNER}/{REPO}/wiki" if WIKI_PUSHED else "\n📚 Wiki: (스킵됨)"
    message = json.dumps({
        "text": f"✅ *code-explainer 완료*\n\n📌 도메인: [도메인명]\n📂 분석 대상: [경로]\n\n*생성된 문서:*\n• 🏗️ L1: 1개\n• 📦 L2: N개 (디렉토리)\n• 🔬 L3: N개 (소스 파일)\n\n💬 Discussions: https://github.com/{OWNER}/{REPO}/discussions{wiki_line}\nLabels: L1, L2, L3, [프로젝트명]"
    })
    req = urllib.request.Request(WEBHOOK_URL, data=message.encode(), headers={"Content-Type": "application/json"})
    urllib.request.urlopen(req)
else:
    print("ℹ️ webhook_url이 비어있어 Google Chat 알림을 스킵합니다.")
```

---

## 버전 관리

Discussion 본문의 `> 리뷰 기준:` 라인 아래에 버전 정보를 표시합니다.

```
> 리뷰 기준: #6 설계 검토, #9 네이밍
> 📌 v2 · 2026-05-08 11:32:04
```

- 신규 생성: `v1`
- 업데이트: 기존 버전에서 +1 (v1→v2→v3...)
- 타임스탬프: `YYYY-MM-DD HH:MM:SS` (초 단위)

---

## 자동 실행 규칙 (중간 확인 없음)

코드 분석 요청("분석해줘", "설명해줘", Git URL, 폴더/파일 경로 제공)을 받으면 **확인 없이 끝까지 자동 실행**합니다.

```
자동 실행 파이프라인 (중간 확인 없음):
  1. Phase 0: 설정 확인 (config.json + gh 인증 + Discussion 활성화 + Wiki 초기화)
  2. 코드 분석 + L1/L2/L3 문서 생성 + 업로드 스크립트 생성
  3. 업로드 스크립트 실행 → Discussion 생성/업데이트 + Label 부착
  3.5. Wiki 적재 → wiki repo clone + 페이지 파일 작성 + Home/Sidebar 생성 + push
       (Wiki 사전 조건 실패 시 이 단계만 스킵)
  4. PR 코멘트 업데이트 (PR이 있을 때만)
  5. Google Chat 알림 전송 (webhook_url이 비어있으면 스킵)
  6. 결과 요약 출력
```

> ⚠️ "실행할까요?", "업로드할까요?" 같은 확인을 **절대 하지 않습니다**.
> 분석 요청을 받은 시점에서 Discussion + Wiki 업로드 + Google Chat 알림까지 한 번에 완료합니다.

---

## 실행 체크리스트

- [ ] Phase 0: 설정 확인 (config.json + gh 인증 + Discussion 활성화 + Wiki 초기화 체크)
- [ ] Step 1~2: 코드 스캔 + 읽기
- [ ] Step 3~5: L1 → L2 (디렉토리별) → L3 (파일별) 문서 생성
- [ ] Phase 3: Label 생성 + Discussion 적재 + Label 부착 완료
- [ ] Phase 3.5: Wiki repo clone + 페이지 파일 작성 + Home/Sidebar 생성 + push (사전 조건 실패 시 스킵)
- [ ] Phase 4: PR 코멘트 (해당 시) — Discussion + Wiki Home 링크 포함
- [ ] Phase 5: Google Chat 알림 — webhook_url 비어있으면 스킵

**결과**: 생성된 Discussion URL 전체 목록 + Wiki Home URL + Google Chat 전송 결과

---

## Tips

- **디렉토리 = Discussion = Wiki 페이지**: 프로젝트 탐색하듯 Discussion 또는 Wiki를 탐색할 수 있어야 합니다.
- **L3는 예외 없이 전부**: DTO든 Router든 모든 파일에 대해 생성합니다. 주니어는 어떤 파일이 "단순"한지 모릅니다.
- **레벨 지정 가능**: "L3만 뽑아줘", "account 모듈만 설명해줘" 처럼 범위를 좁힐 수 있습니다.
- **증분 모드**: 파일 하나만 주면 해당 L3만 생성/업데이트하고 기존 L1·L2는 재사용합니다. Wiki도 동일 파일만 갱신.
- **수동 세팅 최소**: Discussion은 General 카테고리 + Label 방식이라 활성화만 되어 있으면 바로 작동. Wiki는 첫 페이지 1개만 UI에서 만들면 그 이후로는 자동.
- **Discussion vs Wiki 어느 쪽이 더 좋나?**: Discussion은 라벨 검색·코멘트 토론에 강점, Wiki는 사이드바 네비·git 이력 추적에 강점. 둘 다 올려두면 사용자가 선호하는 쪽에서 학습 가능.
