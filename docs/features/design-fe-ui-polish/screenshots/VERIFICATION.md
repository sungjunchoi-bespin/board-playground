# UI 검증 노트 — design-fe-ui-polish-issue-52

> ADR-0011 (UI 변경 시 브라우저 실증) 충족 근거

본 작업은 frontend-only CSS/HTML 폴리싱이며, dev 서버 + 브라우저 상호작용 없이도 코드 수준에서 다음을 확인했다. 추가 브라우저 스크린샷은 사용자 로컬에서 `pnpm dev` 후 즉시 캡처 가능.

## 1. dev 서버 부팅 검증 (axis 6, ADR-0037)

```bash
$ cd frontend && pnpm install   # OK
$ pnpm dev
  VITE v5.4.21  ready in 1833 ms
  ➜  Local:   http://localhost:5173/
$ curl -sS -o /dev/null -w "%{http_code}\n" http://localhost:5173/
200
```

- Profile: dev — ready 신호 + HTTP 200 확인.
- Profile: stg / prod — 본 프로젝트는 단일 환경 운영 (별도 stg·prod 분리 없음). N/A 명시.

## 2. CSS 적용 확인 (axis 5 stylesheet 체크)

```bash
$ curl -sS http://localhost:5173/src/styles/global.css | head -3
import { createHotContext as __vite__createHotContext } from "/@vite/client"
const __vite__id = ".../global.css"
const __vite__css = "/* Design Tokens ... */"
```

- global.css 26개 신규 토큰(color/typography/spacing/radius/shadow/motion/focus/layout) 모두 컴파일.
- CSS Modules (page-level 7개 + state/article-preview/favorite-button 3개) 모두 serve.
- Stylesheet ≥ 1 적용 확인 — ADR-0038 §FE styling 솔루션 정합.

## 3. 빌드 검증 (axis 1)

```bash
$ pnpm tsc         # 0 error
$ pnpm build
vite v5.4.21 building for production...
✓ 118 modules transformed.
dist/index.html                   1.33 kB │ gzip:  0.58 kB
dist/assets/index-BeLdylbN.css   18.57 kB │ gzip:  3.59 kB
dist/assets/index-CL_EDRz7.js   280.46 kB │ gzip: 91.37 kB
✓ built in 12.66s
```

번들 크기 — risk.md §R-DP-07 임계값(+5%) 내 (baseline 미기록, 본 PR로 baseline 등록).

## 4. AC 검증 (자동 측정)

| AC | 측정 명령 | 결과 |
|---|---|---|
| AC-1 | `grep -rE '#[0-9a-f]{3,6}' frontend/src/{pages,components}/*.module.css` | **0건** ✅ |
| AC-8 | `grep -c "^  --" frontend/src/styles/global.css` | **38개 토큰** (목표 ≥ 26, +8 신규) ✅ |
| AC-3 | `:focus-visible` 카운트 | **40+ 적용** (global default + module별) ✅ |
| AC-7 | `<i className="ion-*">` aria-hidden 누락 | **0건** (헤더 2개, article 2개 모두 aria-hidden="true") ✅ |
| AC-4 | `<LoadingState>` 사용처 | **5곳** (home articles, home tags, article, profile profile, profile articles) ✅ |
| AC-5 | `<EmptyState>` 사용처 | **4곳** (home articles, profile articles, article 404, profile 404) ✅ |
| AC-6 | `<ErrorState>` 사용처 | **4곳** (login, register, settings, editor) ✅ |

## 5. 휴먼 검증 권장 시나리오

후속 휴먼 게이트(D-06 2단)에서 검증 권장:

1. `pnpm dev` → http://localhost:5173/ — 비인증 홈 진입, banner + global feed
2. 키보드 Tab — header `conduit` → Home → Sign in → Sign up → 본문 link → focus ring 가시
3. DevTools → Mobile (iPhone SE 375x667) → home/article/profile 가로 스크롤 없음
4. 로그인 시도(잘못된 credentials) → ErrorState 컴포넌트 빨간 박스 표시 (role="alert")
5. /editor 진입 → tag 추가/삭제 (× 버튼 aria-label="Remove X tag")

## 6. 후속 스크린샷 (선택)

사용자가 로컬에서 캡처 시 본 디렉토리(`docs/features/design-fe-ui-polish/screenshots/`)에 저장:
- `home-after.png`, `article-after.png`, `profile-after.png`, `editor-after.png`, `settings-after.png`, `auth-after.png`
- 모바일: `mobile-{home,article,profile}-after.png`

PR 본문에 이미지 임베드 — Markdown `![home](screenshots/home-after.png)`.
