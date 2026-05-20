# Conduit — RealWorld Medium Clone

A full-stack implementation of the [RealWorld](https://realworld-docs.netlify.app/) "Conduit" blogging platform.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18 + TypeScript 5.6 + Vite 5 + CSS Modules |
| Backend | Java 24 + Spring Boot 3.5 + Hexagonal Architecture |
| Database | PostgreSQL 16 (H2 for local dev) |
| Auth | JWT (stateless, `Token` prefix) |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Playwright E2E |

## Architecture

```
backend/                       # Spring Boot (Hexagonal / Ports & Adapters)
  src/main/java/com/conduit/
    user/                      # User module (register, login, profile update)
    article/                   # Article module (CRUD, feed, favorites)
    comment/                   # Comment module (add, list, delete)
    profile/                   # Profile module (get, follow, unfollow)
    tag/                       # Tag module (list popular tags)
    shared/                    # Cross-cutting (security, config, exceptions)

frontend/                      # React SPA
  src/
    pages/                     # Route pages (home, article, editor, auth, settings, profile)
    components/                # Shared components (favorite button, etc.)
    api/                       # Axios API clients
    hooks/                     # Custom hooks (useAuth)
```

## Quick Start

### Prerequisites

- Java 24+
- Node.js 20 LTS + pnpm 9
- (Optional) PostgreSQL 16 for dev/stg/prod profiles

### Run locally (H2 in-memory DB)

```bash
# Backend (terminal 1)
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
# -> http://localhost:8080

# Frontend (terminal 2)
cd frontend
pnpm install
pnpm dev
# -> http://localhost:5174
```

### API Documentation

With the backend running, open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for the Swagger UI.

## API Endpoints (19 total)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/users` | - | Register |
| POST | `/api/users/login` | - | Login |
| GET | `/api/user` | Required | Get current user |
| PUT | `/api/user` | Required | Update profile |
| GET | `/api/articles` | - | List articles (filter by tag/author/favorited) |
| GET | `/api/articles/feed` | Required | Feed (from followed users) |
| POST | `/api/articles` | Required | Create article |
| GET | `/api/articles/:slug` | - | Get article |
| PUT | `/api/articles/:slug` | Required | Update article (author only) |
| DELETE | `/api/articles/:slug` | Required | Delete article (author only) |
| POST | `/api/articles/:slug/favorite` | Required | Favorite article |
| DELETE | `/api/articles/:slug/favorite` | Required | Unfavorite article |
| POST | `/api/articles/:slug/comments` | Required | Add comment |
| GET | `/api/articles/:slug/comments` | - | List comments |
| DELETE | `/api/articles/:slug/comments/:id` | Required | Delete comment (author only) |
| GET | `/api/profiles/:username` | - | Get profile |
| POST | `/api/profiles/:username/follow` | Required | Follow user |
| DELETE | `/api/profiles/:username/follow` | Required | Unfollow user |
| GET | `/api/tags` | - | List tags |

## Testing

```bash
# Backend unit + integration tests
cd backend
./gradlew test

# Frontend E2E tests (requires both FE + BE running)
cd frontend
pnpm exec playwright test
```

## Profiles

| Profile | DB | Logging | Use case |
|---------|-----|---------|----------|
| `local` | H2 in-memory | DEBUG | Quick local dev (no PostgreSQL needed) |
| `dev` | PostgreSQL `conduit_dev` | DEBUG | Full dev with persistent DB |
| `stg` | PostgreSQL `conduit_stg` | INFO | Staging |
| `prod` | PostgreSQL `conduit_prod` | WARN | Production |

See [LOCAL.md](LOCAL.md) for detailed per-profile setup instructions.
