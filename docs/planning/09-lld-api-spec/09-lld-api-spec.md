---
doc_type: api-spec
gate: C
version: v1.0
date: 2026-05-18
status: Draft
author: sungjun.choi@board-playground.dev
related:
  R-ID: [R-F-01, R-F-02, R-F-03, R-F-04, R-F-05, R-F-06, R-F-07, R-F-08, R-F-09, R-F-10, R-F-11, R-F-12, R-F-13, R-F-14, R-F-15, R-F-16, R-F-17, R-F-18, R-F-19]
  F-ID: [F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10]
  supersedes: null
---

# Conduit (RealWorld) — API Spec (LLD — 외부 인터페이스)

## 변경 이력

| Version | Date | Author | Change |
|---|---|---|---|
| v1.0 | 2026-05-18 | Agent (architect) | 초안 — RealWorld 공식 스펙 기반 19개 엔드포인트 정의 |

## 1. 개요

### Base URL

```
/api
```

모든 엔드포인트는 `/api` prefix를 사용한다. 로컬 개발 환경 기준 `http://localhost:3000/api`.

### Content-Type

모든 요청과 응답은 `application/json; charset=utf-8`을 사용한다.

### 인증 (Authentication)

JWT 기반 인증. 인증이 필요한 엔드포인트는 다음 헤더를 포함해야 한다.

```
Authorization: Token jwt.token.here
```

- `Token` prefix는 RealWorld 스펙 고유 형식 (Bearer가 아님)
- 인증 필수(Required): 헤더 누락 시 `401 Unauthorized`
- 인증 선택(Optional): 헤더 있으면 사용자 컨텍스트 반영, 없으면 비인증 모드로 처리

### 에러 응답 형식

모든 에러 응답은 다음 형식을 따른다.

```json
{
  "errors": {
    "body": ["can't be blank"],
    "email": ["has already been taken"]
  }
}
```

### 공통 응답 객체

**User 객체**

```json
{
  "user": {
    "email": "jake@jake.jake",
    "token": "jwt.token.here",
    "username": "jake",
    "bio": "I work at statefarm",
    "image": "https://i.stack.imgur.com/xHWG8.jpg"
  }
}
```

**Profile 객체**

```json
{
  "profile": {
    "username": "jake",
    "bio": "I work at statefarm",
    "image": "https://i.stack.imgur.com/xHWG8.jpg",
    "following": false
  }
}
```

**Article 객체**

```json
{
  "article": {
    "slug": "how-to-train-your-dragon",
    "title": "How to train your dragon",
    "description": "Ever wonder how?",
    "body": "It takes a Lifetime",
    "tagList": ["dragons", "training"],
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T03:48:35.824Z",
    "favorited": false,
    "favoritesCount": 0,
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

**Comment 객체**

```json
{
  "comment": {
    "id": 1,
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T03:22:56.637Z",
    "body": "It takes a Lifetime",
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

## 2. 엔드포인트 목록

| # | 메서드 | 경로 | 목적 | F-ID | R-ID | Auth |
|---|---|---|---|---|---|---|
| 1 | POST | /api/users | 회원가입 | F-01 | R-F-01 | N/A |
| 2 | POST | /api/users/login | 로그인 | F-02 | R-F-02 | N/A |
| 3 | GET | /api/user | 현재 사용자 조회 | F-02 | R-F-03 | Required |
| 4 | PUT | /api/user | 사용자 정보 수정 | F-03 | R-F-04 | Required |
| 5 | GET | /api/profiles/:username | 프로필 조회 | F-03 | R-F-05 | Optional |
| 6 | POST | /api/profiles/:username/follow | 팔로우 | F-04 | R-F-06 | Required |
| 7 | DELETE | /api/profiles/:username/follow | 언팔로우 | F-04 | R-F-07 | Required |
| 8 | GET | /api/articles | 아티클 목록 조회 | F-05 | R-F-08 | Optional |
| 9 | GET | /api/articles/feed | 피드 조회 | F-06 | R-F-09 | Required |
| 10 | GET | /api/articles/:slug | 아티클 단건 조회 | F-07 | R-F-10 | Optional |
| 11 | POST | /api/articles | 아티클 생성 | F-07 | R-F-11 | Required |
| 12 | PUT | /api/articles/:slug | 아티클 수정 | F-07 | R-F-12 | Required |
| 13 | DELETE | /api/articles/:slug | 아티클 삭제 | F-07 | R-F-13 | Required |
| 14 | POST | /api/articles/:slug/comments | 댓글 추가 | F-08 | R-F-14 | Required |
| 15 | GET | /api/articles/:slug/comments | 댓글 목록 조회 | F-08 | R-F-15 | Optional |
| 16 | DELETE | /api/articles/:slug/comments/:id | 댓글 삭제 | F-08 | R-F-16 | Required |
| 17 | POST | /api/articles/:slug/favorite | 즐겨찾기 추가 | F-09 | R-F-17 | Required |
| 18 | DELETE | /api/articles/:slug/favorite | 즐겨찾기 해제 | F-09 | R-F-18 | Required |
| 19 | GET | /api/tags | 태그 목록 조회 | F-10 | R-F-19 | N/A |

## 3. 엔드포인트 상세

---

### 3.1 POST /api/users

> **회원가입** | F-01 | R-F-01

**Request**

- Method: `POST`
- Path: `/api/users`
- Headers: `Content-Type: application/json`
- Auth: N/A
- Body:

```json
{
  "user": {
    "username": "jacob",
    "email": "jake@jake.jake",
    "password": "jakejake"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| user.username | string | Y | 고유 사용자명 |
| user.email | string | Y | 고유 이메일 |
| user.password | string | Y | 비밀번호 (평문, 서버에서 해싱) |

**Response 200**

```json
{
  "user": {
    "email": "jake@jake.jake",
    "token": "jwt.token.here",
    "username": "jacob",
    "bio": null,
    "image": null
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 422 | email 중복 | `{ "errors": { "email": ["has already been taken"] } }` |
| 422 | username 중복 | `{ "errors": { "username": ["has already been taken"] } }` |
| 422 | 필수 필드 누락 | `{ "errors": { "email": ["can't be blank"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 유효한 username/email/password로 가입 -> 200 + JWT 포함 User 객체
- 실패: 이미 등록된 email로 가입 시도 -> 422 + email 에러
- 실패: 이미 등록된 username으로 가입 시도 -> 422 + username 에러
- 실패: password 누락 -> 422 + password can't be blank
- 실패: email 형식 부적합 -> 422 + email is invalid

---

### 3.2 POST /api/users/login

> **로그인** | F-02 | R-F-02

**Request**

- Method: `POST`
- Path: `/api/users/login`
- Headers: `Content-Type: application/json`
- Auth: N/A
- Body:

```json
{
  "user": {
    "email": "jake@jake.jake",
    "password": "jakejake"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| user.email | string | Y | 등록된 이메일 |
| user.password | string | Y | 비밀번호 |

**Response 200**

```json
{
  "user": {
    "email": "jake@jake.jake",
    "token": "jwt.token.here",
    "username": "jake",
    "bio": "I work at statefarm",
    "image": null
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | 잘못된 email 또는 password | `{ "errors": { "email or password": ["is invalid"] } }` |
| 422 | 필수 필드 누락 | `{ "errors": { "email": ["can't be blank"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 올바른 email/password -> 200 + JWT 포함 User 객체
- 실패: 잘못된 password -> 401
- 실패: 미등록 email -> 401
- 실패: email 누락 -> 422

---

### 3.3 GET /api/user

> **현재 사용자 조회** | F-02 | R-F-03

**Request**

- Method: `GET`
- Path: `/api/user`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Body: 없음

**Response 200**

```json
{
  "user": {
    "email": "jake@jake.jake",
    "token": "jwt.token.here",
    "username": "jake",
    "bio": "I work at statefarm",
    "image": null
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 유효 JWT -> 200 + 현재 User 객체 (token 필드에 갱신 JWT 포함 가능)
- 실패: Authorization 헤더 누락 -> 401
- 실패: 만료된 JWT -> 401
- 실패: 변조된 JWT -> 401

---

### 3.4 PUT /api/user

> **사용자 정보 수정** | F-03 | R-F-04

**Request**

- Method: `PUT`
- Path: `/api/user`
- Headers: `Authorization: Token jwt.token.here`, `Content-Type: application/json`
- Auth: Required
- Body:

```json
{
  "user": {
    "email": "jake@jake.jake",
    "bio": "I like to skateboard",
    "image": "https://i.stack.imgur.com/xHWG8.jpg"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| user.email | string | N | 변경할 이메일 |
| user.username | string | N | 변경할 사용자명 |
| user.password | string | N | 변경할 비밀번호 |
| user.image | string | N | 프로필 이미지 URL |
| user.bio | string | N | 자기소개 |

> 모든 필드 선택적. 전달된 필드만 갱신 (partial update).

**Response 200**

```json
{
  "user": {
    "email": "jake@jake.jake",
    "token": "jwt.token.here",
    "username": "jake",
    "bio": "I like to skateboard",
    "image": "https://i.stack.imgur.com/xHWG8.jpg"
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 422 | 중복 email | `{ "errors": { "email": ["has already been taken"] } }` |
| 422 | 중복 username | `{ "errors": { "username": ["has already been taken"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: bio만 수정 -> 200 + bio 갱신된 User 객체
- 정상: password 수정 -> 200 + 이후 새 password로 로그인 가능
- 정상: image URL 수정 -> 200 + image 반영
- 실패: 타인의 email과 중복 -> 422
- 실패: 미인증 -> 401

---

### 3.5 GET /api/profiles/:username

> **프로필 조회** | F-03 | R-F-05

**Request**

- Method: `GET`
- Path: `/api/profiles/:username`
- Headers: `Authorization: Token jwt.token.here` (Optional)
- Auth: Optional
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| username | string | 조회 대상 사용자명 |

**Response 200**

```json
{
  "profile": {
    "username": "jake",
    "bio": "I work at statefarm",
    "image": "https://i.stack.imgur.com/xHWG8.jpg",
    "following": false
  }
}
```

> `following`: 인증된 사용자가 해당 프로필을 팔로우 중인지 여부. 비인증 시 항상 `false`.

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 404 | 존재하지 않는 username | `{ "errors": { "username": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 존재하는 username 비인증 조회 -> 200 + Profile (following=false)
- 정상: 팔로우 중인 사용자를 인증 조회 -> 200 + Profile (following=true)
- 정상: 팔로우하지 않는 사용자를 인증 조회 -> 200 + Profile (following=false)
- 실패: 미존재 username -> 404

---

### 3.6 POST /api/profiles/:username/follow

> **팔로우** | F-04 | R-F-06

**Request**

- Method: `POST`
- Path: `/api/profiles/:username/follow`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| username | string | 팔로우 대상 사용자명 |

- Body: 없음

**Response 200**

```json
{
  "profile": {
    "username": "jake",
    "bio": "I work at statefarm",
    "image": "https://i.stack.imgur.com/xHWG8.jpg",
    "following": true
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 404 | 존재하지 않는 username | `{ "errors": { "username": ["not found"] } }` |
| 422 | 이미 팔로우 중 (멱등 처리 시 200) | `{ "errors": { "username": ["already following"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 타인 팔로우 -> 200 + Profile (following=true)
- 정상: 이미 팔로우 중인 사용자 재팔로우 -> 200 (멱등)
- 실패: 미인증 -> 401
- 실패: 미존재 사용자 팔로우 -> 404

---

### 3.7 DELETE /api/profiles/:username/follow

> **언팔로우** | F-04 | R-F-07

**Request**

- Method: `DELETE`
- Path: `/api/profiles/:username/follow`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| username | string | 언팔로우 대상 사용자명 |

- Body: 없음

**Response 200**

```json
{
  "profile": {
    "username": "jake",
    "bio": "I work at statefarm",
    "image": "https://i.stack.imgur.com/xHWG8.jpg",
    "following": false
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 404 | 존재하지 않는 username | `{ "errors": { "username": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 팔로우 중인 사용자 언팔로우 -> 200 + Profile (following=false)
- 정상: 이미 언팔로우 상태에서 재호출 -> 200 (멱등)
- 실패: 미인증 -> 401
- 실패: 미존재 사용자 -> 404

---

### 3.8 GET /api/articles

> **아티클 목록 조회 (글로벌 피드)** | F-05 | R-F-08

**Request**

- Method: `GET`
- Path: `/api/articles`
- Headers: `Authorization: Token jwt.token.here` (Optional)
- Auth: Optional (인증 시 favorited 필드 반영)
- Query Params:

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| tag | string | - | 태그 필터 |
| author | string | - | 작성자 username 필터 |
| favorited | string | - | 즐겨찾기한 사용자 username 필터 |
| limit | number | 20 | 반환 건수 |
| offset | number | 0 | 건너뛸 건수 |

**Response 200**

```json
{
  "articles": [
    {
      "slug": "how-to-train-your-dragon",
      "title": "How to train your dragon",
      "description": "Ever wonder how?",
      "body": "It takes a Lifetime",
      "tagList": ["dragons", "training"],
      "createdAt": "2016-02-18T03:22:56.637Z",
      "updatedAt": "2016-02-18T03:48:35.824Z",
      "favorited": false,
      "favoritesCount": 0,
      "author": {
        "username": "jake",
        "bio": "I work at statefarm",
        "image": "https://i.stack.imgur.com/xHWG8.jpg",
        "following": false
      }
    }
  ],
  "articlesCount": 1
}
```

> **참고** (2024/08/16 이후): List Articles에서 `body` 필드를 미반환할 수 있다. 본 프로젝트에서는 SRS v1.1 결정에 따라 body 미반환을 기본으로 한다. 단건 조회(GET /api/articles/:slug)에서만 body를 반환한다.

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

> 필터 결과 0건인 경우 에러가 아닌 빈 배열 + articlesCount=0 반환.

**테스트 시나리오**

- 정상: 전체 조회 -> 200 + 최신순 articles[] + articlesCount
- 정상: tag=dragons 필터 -> 해당 태그 아티클만 반환
- 정상: author=jake 필터 -> jake의 아티클만 반환
- 정상: favorited=jake 필터 -> jake가 즐겨찾기한 아티클만 반환
- 정상: limit=5&offset=10 -> 페이지네이션 적용
- 정상: 결과 0건 -> 200 + articles=[] + articlesCount=0
- 정상: 인증 상태 -> favorited 필드 현재 사용자 기준 반영

---

### 3.9 GET /api/articles/feed

> **피드 조회 (개인 피드)** | F-06 | R-F-09

**Request**

- Method: `GET`
- Path: `/api/articles/feed`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Query Params:

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| limit | number | 20 | 반환 건수 |
| offset | number | 0 | 건너뛸 건수 |

**Response 200**

```json
{
  "articles": [
    {
      "slug": "how-to-train-your-dragon",
      "title": "How to train your dragon",
      "description": "Ever wonder how?",
      "tagList": ["dragons", "training"],
      "createdAt": "2016-02-18T03:22:56.637Z",
      "updatedAt": "2016-02-18T03:48:35.824Z",
      "favorited": false,
      "favoritesCount": 0,
      "author": {
        "username": "jake",
        "bio": "I work at statefarm",
        "image": "https://i.stack.imgur.com/xHWG8.jpg",
        "following": true
      }
    }
  ],
  "articlesCount": 1
}
```

> 팔로잉 사용자의 아티클만 최신순 반환. body 미반환 (List와 동일 정책).

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 팔로잉 사용자의 글 -> 200 + 피드에 포함
- 정상: 팔로잉 0명 -> 200 + articles=[] + articlesCount=0
- 정상: limit/offset 페이지네이션 적용
- 실패: 미인증 -> 401

---

### 3.10 GET /api/articles/:slug

> **아티클 단건 조회** | F-07 | R-F-10

**Request**

- Method: `GET`
- Path: `/api/articles/:slug`
- Headers: `Authorization: Token jwt.token.here` (Optional)
- Auth: Optional
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 아티클 slug |

**Response 200**

```json
{
  "article": {
    "slug": "how-to-train-your-dragon",
    "title": "How to train your dragon",
    "description": "Ever wonder how?",
    "body": "It takes a Lifetime",
    "tagList": ["dragons", "training"],
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T03:48:35.824Z",
    "favorited": false,
    "favoritesCount": 0,
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

> 단건 조회는 `body` 필드를 포함한다.

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 404 | 존재하지 않는 slug | `{ "errors": { "article": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 존재하는 slug -> 200 + Article (body 포함)
- 정상: 인증 상태 -> favorited/following 현재 사용자 기준 반영
- 실패: 미존재 slug -> 404

---

### 3.11 POST /api/articles

> **아티클 생성** | F-07 | R-F-11

**Request**

- Method: `POST`
- Path: `/api/articles`
- Headers: `Authorization: Token jwt.token.here`, `Content-Type: application/json`
- Auth: Required
- Body:

```json
{
  "article": {
    "title": "How to train your dragon",
    "description": "Ever wonder how?",
    "body": "You have to believe",
    "tagList": ["reactjs", "angularjs", "dragons"]
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| article.title | string | Y | 아티클 제목 |
| article.description | string | Y | 아티클 요약 |
| article.body | string | Y | 아티클 본문 (마크다운) |
| article.tagList | string[] | N | 태그 목록 (없으면 빈 배열) |

**Response 200**

```json
{
  "article": {
    "slug": "how-to-train-your-dragon",
    "title": "How to train your dragon",
    "description": "Ever wonder how?",
    "body": "You have to believe",
    "tagList": ["reactjs", "angularjs", "dragons"],
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T03:22:56.637Z",
    "favorited": false,
    "favoritesCount": 0,
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

> slug는 title로부터 자동 생성 (lowercase + hyphen). 중복 시 고유성 보장 (suffix 추가).

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 422 | title 누락 | `{ "errors": { "title": ["can't be blank"] } }` |
| 422 | description 누락 | `{ "errors": { "description": ["can't be blank"] } }` |
| 422 | body 누락 | `{ "errors": { "body": ["can't be blank"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 전체 필드(tagList 포함) -> 200 + Article + slug 자동 생성
- 정상: tagList 생략 -> 200 + tagList=[]
- 정상: 동일 title 중복 생성 -> 200 + 고유 slug 보장
- 실패: title 누락 -> 422
- 실패: body 누락 -> 422
- 실패: 미인증 -> 401

---

### 3.12 PUT /api/articles/:slug

> **아티클 수정** | F-07 | R-F-12

**Request**

- Method: `PUT`
- Path: `/api/articles/:slug`
- Headers: `Authorization: Token jwt.token.here`, `Content-Type: application/json`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 수정 대상 아티클 slug |

- Body:

```json
{
  "article": {
    "title": "Did you train your dragon?"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| article.title | string | N | 변경할 제목 (변경 시 slug 재생성) |
| article.description | string | N | 변경할 요약 |
| article.body | string | N | 변경할 본문 |

> 모든 필드 선택적. 전달된 필드만 갱신 (partial update). title 변경 시 slug도 재생성.

**Response 200**

```json
{
  "article": {
    "slug": "did-you-train-your-dragon",
    "title": "Did you train your dragon?",
    "description": "Ever wonder how?",
    "body": "You have to believe",
    "tagList": ["reactjs", "angularjs", "dragons"],
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T05:30:00.000Z",
    "favorited": false,
    "favoritesCount": 0,
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 403 | 타인의 아티클 수정 시도 | `{ "errors": { "article": ["not owned by you"] } }` |
| 404 | 존재하지 않는 slug | `{ "errors": { "article": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: title 수정 -> 200 + slug 재생성 + updatedAt 갱신
- 정상: body만 수정 -> 200 + body 갱신, slug 유지
- 실패: 타인 아티클 수정 시도 -> 403
- 실패: 미존재 slug -> 404
- 실패: 미인증 -> 401

---

### 3.13 DELETE /api/articles/:slug

> **아티클 삭제** | F-07 | R-F-13

**Request**

- Method: `DELETE`
- Path: `/api/articles/:slug`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 삭제 대상 아티클 slug |

- Body: 없음

**Response 200**

```
HTTP 200 OK (빈 body 또는 204 No Content)
```

> 아티클 삭제 시 연관 댓글, 즐겨찾기, 태그 매핑도 함께 삭제 (cascade).

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 403 | 타인의 아티클 삭제 시도 | `{ "errors": { "article": ["not owned by you"] } }` |
| 404 | 존재하지 않는 slug | `{ "errors": { "article": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 자기 아티클 삭제 -> 200/204 + 연관 댓글/즐겨찾기도 삭제
- 실패: 타인 아티클 삭제 시도 -> 403
- 실패: 미존재 slug -> 404
- 실패: 미인증 -> 401

---

### 3.14 POST /api/articles/:slug/comments

> **댓글 추가** | F-08 | R-F-14

**Request**

- Method: `POST`
- Path: `/api/articles/:slug/comments`
- Headers: `Authorization: Token jwt.token.here`, `Content-Type: application/json`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 아티클 slug |

- Body:

```json
{
  "comment": {
    "body": "His name was my name too."
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| comment.body | string | Y | 댓글 본문 |

**Response 200**

```json
{
  "comment": {
    "id": 1,
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T03:22:56.637Z",
    "body": "His name was my name too.",
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 404 | 존재하지 않는 slug | `{ "errors": { "article": ["not found"] } }` |
| 422 | body 누락 또는 빈 문자열 | `{ "errors": { "body": ["can't be blank"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 유효한 body로 댓글 작성 -> 200 + Comment 객체
- 실패: body 빈 문자열 -> 422
- 실패: 미존재 slug -> 404
- 실패: 미인증 -> 401

---

### 3.15 GET /api/articles/:slug/comments

> **댓글 목록 조회** | F-08 | R-F-15

**Request**

- Method: `GET`
- Path: `/api/articles/:slug/comments`
- Headers: `Authorization: Token jwt.token.here` (Optional)
- Auth: Optional (인증 시 following 필드 반영)
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 아티클 slug |

**Response 200**

```json
{
  "comments": [
    {
      "id": 1,
      "createdAt": "2016-02-18T03:22:56.637Z",
      "updatedAt": "2016-02-18T03:22:56.637Z",
      "body": "It takes a Lifetime",
      "author": {
        "username": "jake",
        "bio": "I work at statefarm",
        "image": "https://i.stack.imgur.com/xHWG8.jpg",
        "following": false
      }
    }
  ]
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 404 | 존재하지 않는 slug | `{ "errors": { "article": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 댓글 존재 -> 200 + comments[] (최신순)
- 정상: 댓글 0건 -> 200 + comments=[]
- 정상: 인증 시 -> author.following 현재 사용자 기준 반영
- 실패: 미존재 slug -> 404

---

### 3.16 DELETE /api/articles/:slug/comments/:id

> **댓글 삭제** | F-08 | R-F-16

**Request**

- Method: `DELETE`
- Path: `/api/articles/:slug/comments/:id`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 아티클 slug |
| id | number | 댓글 ID |

- Body: 없음

**Response 200**

```
HTTP 200 OK (빈 body 또는 204 No Content)
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 403 | 타인의 댓글 삭제 시도 | `{ "errors": { "comment": ["not owned by you"] } }` |
| 404 | 존재하지 않는 slug 또는 id | `{ "errors": { "comment": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 자기 댓글 삭제 -> 200/204
- 실패: 타인 댓글 삭제 시도 -> 403
- 실패: 미존재 댓글 ID -> 404
- 실패: 미인증 -> 401

---

### 3.17 POST /api/articles/:slug/favorite

> **즐겨찾기 추가** | F-09 | R-F-17

**Request**

- Method: `POST`
- Path: `/api/articles/:slug/favorite`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 아티클 slug |

- Body: 없음

**Response 200**

```json
{
  "article": {
    "slug": "how-to-train-your-dragon",
    "title": "How to train your dragon",
    "description": "Ever wonder how?",
    "body": "It takes a Lifetime",
    "tagList": ["dragons", "training"],
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T03:48:35.824Z",
    "favorited": true,
    "favoritesCount": 1,
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 404 | 존재하지 않는 slug | `{ "errors": { "article": ["not found"] } }` |
| 422 | 이미 즐겨찾기 상태 (멱등 처리 시 200) | `{ "errors": { "article": ["already favorited"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 즐겨찾기 추가 -> 200 + favorited=true + favoritesCount 증가
- 정상: 이미 즐겨찾기 -> 200 (멱등, favoritesCount 변동 없음)
- 실패: 미존재 slug -> 404
- 실패: 미인증 -> 401

---

### 3.18 DELETE /api/articles/:slug/favorite

> **즐겨찾기 해제** | F-09 | R-F-18

**Request**

- Method: `DELETE`
- Path: `/api/articles/:slug/favorite`
- Headers: `Authorization: Token jwt.token.here`
- Auth: Required
- Path Params:

| 파라미터 | 타입 | 설명 |
|---|---|---|
| slug | string | 아티클 slug |

- Body: 없음

**Response 200**

```json
{
  "article": {
    "slug": "how-to-train-your-dragon",
    "title": "How to train your dragon",
    "description": "Ever wonder how?",
    "body": "It takes a Lifetime",
    "tagList": ["dragons", "training"],
    "createdAt": "2016-02-18T03:22:56.637Z",
    "updatedAt": "2016-02-18T03:48:35.824Z",
    "favorited": false,
    "favoritesCount": 0,
    "author": {
      "username": "jake",
      "bio": "I work at statefarm",
      "image": "https://i.stack.imgur.com/xHWG8.jpg",
      "following": false
    }
  }
}
```

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 401 | JWT 누락 또는 무효 | `{ "errors": { "token": ["is invalid"] } }` |
| 404 | 존재하지 않는 slug | `{ "errors": { "article": ["not found"] } }` |
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 즐겨찾기 해제 -> 200 + favorited=false + favoritesCount 감소
- 정상: 이미 해제 상태 -> 200 (멱등, favoritesCount 변동 없음)
- 실패: 미존재 slug -> 404
- 실패: 미인증 -> 401

---

### 3.19 GET /api/tags

> **태그 목록 조회** | F-10 | R-F-19

**Request**

- Method: `GET`
- Path: `/api/tags`
- Headers: 없음
- Auth: N/A
- Body: 없음

**Response 200**

```json
{
  "tags": ["reactjs", "angularjs", "dragons", "training"]
}
```

> 사용 중인 태그 목록 반환. 아티클에 한 번이라도 사용된 태그만 포함.

**Response 4xx/5xx**

| 상태코드 | 조건 | 응답 body |
|---|---|---|
| 500 | 서버 내부 오류 | `{ "errors": { "server": ["internal server error"] } }` |

**테스트 시나리오**

- 정상: 태그 존재 -> 200 + tags[] (문자열 배열)
- 정상: 아티클 0건 (태그 없음) -> 200 + tags=[]
- 실패: 서버 오류 -> 500

## 4. Webhook / 콜백

N/A -- 본 프로젝트는 외부 시스템 연동이 없으며 Webhook/콜백 엔드포인트를 제공하지 않는다.

## 5. Rate Limit / Quota

로컬 전용 실행 환경이므로 Rate Limit를 적용하지 않는다.

- Rate Limit: N/A (로컬 전용)
- Quota: N/A
- 추후 배포 환경 전환 시 별도 설계 필요 (미들웨어 기반 Rate Limit 권장)
