# Codele

**Codele** is a daily coding puzzle web application inspired by Wordle. Players guess hidden identifiers in real source code snippets, gradually reveal the logic, and name the underlying program or algorithm. Administrators manage puzzle content and schedule daily challenges through a secured panel, with all persistent data stored in **PostgreSQL**.

Built as a full-stack university project using **Spring Boot 4**, **Spring Security**, **JPA**, **Flyway**, and a modern **Thymeleaf** frontend.

---

## Table of contents

- [About](#about)
- [Features](#features)
- [How to play](#how-to-play)
- [Screenshots & pages](#screenshots--pages)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick start](#quick-start)
- [Admin guide](#admin-guide)
- [Database](#database)
- [Configuration](#configuration)
- [API overview](#api-overview)
- [Project structure](#project-structure)

---

## About

Codele gamifies reading and understanding source code. Each puzzle presents a partially masked snippet (Java, JavaScript, etc.). Players type guesses for tokens such as variable names, keywords, and function names:

- **Correct guesses** reveal every occurrence of that token in the snippet.
- **Wrong guesses** are tracked so players do not repeat them.
- After enough of the code is decoded (or hints are used), players unlock a **final guess** to name the program.

Daily puzzles are assigned by **calendar date** in the database. Only one puzzle can be scheduled per day. The archive allows free-play on any previously created puzzle.

---

## Features

### Player features

- Daily puzzle with clue and language badge
- Archive of all puzzles
- Token guessing with live code reveal
- Guess history: words **in the code** vs **not in the code**
- Hint system (peek next hidden token)
- Final answer submission and scoring
- User registration and login
- Personal stats and global leaderboard
- Guest play (no account required)

### Admin features

- Secured admin panel (`ROLE_ADMIN` only)
- Create, edit, and delete puzzles
- Schedule a puzzle for a specific date (daily challenge)
- Bulk schedule: assign multiple puzzles to consecutive days
- All content persisted in PostgreSQL

### Engineering features

- PostgreSQL 16 via Docker Compose (local) or Neon (production)
- Flyway versioned schema migrations
- Spring profiles: `local`, `prod`, `test`
- Spring Security with role-based access control
- Health endpoint at `/actuator/health`
- Environment-based configuration (`.env`)

---

## How to play

1. Open the home page and click **Play Daily Puzzle** (or pick one from the archive).
2. Read the masked code snippet and the optional clue.
3. Type a word/token and press **Guess it**.
   - If it appears in the code, all copies light up.
   - If not, it moves to the **Not in the code** list.
4. Use **Peek next word** if you need a hint (costs a hint).
5. When enough is revealed, submit your **final guess** (program name).
6. View your score and explanation at the end.

---

## Screenshots & pages

| Route | Description |
|-------|-------------|
| `/` | Home — daily puzzle preview and CTAs |
| `/play/daily` | Today's scheduled puzzle |
| `/play/{id}` | Play a specific puzzle from the archive |
| `/archive` | Browse all puzzles |
| `/how-to-play` | Rules and instructions |
| `/leaderboard` | Top players |
| `/stats` | Logged-in user statistics |
| `/login` | Sign in (admin or player) |
| `/register` | Create a player account |
| `/admin` | Admin puzzle management (admin only) |

---

## Architecture

```text
┌─────────────┐     HTTP      ┌──────────────────────┐     JDBC      ┌─────────────┐
│   Browser   │ ────────────► │  Spring Boot 4 app   │ ────────────► │ PostgreSQL  │
│  (Thymeleaf)│               │  MVC + REST + Security│               │  (Flyway)   │
└─────────────┘               └──────────────────────┘               └─────────────┘
                                        │
                                        ▼
                              Session-based gameplay
                              Admin CRUD + scheduling
```

**Data flow (daily puzzle):**

1. Admin creates a puzzle and sets `scheduled_date` to today.
2. Home page and `/play/daily` load the puzzle where `scheduled_date = current date`.
3. Gameplay runs in the HTTP session (masked code, guesses, hints).
4. On completion, attempts and stats are saved for logged-in users.

---

## Tech stack

| Layer | Technology |
|--------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| Web | Spring MVC, Thymeleaf, vanilla JavaScript |
| Security | Spring Security (form login, roles) |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Build | Maven |
| Local infra | Docker Compose |
| Cloud (optional) | Neon + Railway — see [DEPLOY.md](DEPLOY.md) |

---

## Prerequisites

- **Java 21** ([Adoptium](https://adoptium.net/) or similar)
- **Docker Desktop** (for local PostgreSQL)
- **Git** (optional, for cloning)

Maven wrapper is included (`mvnw` / `mvnw.cmd`) — no separate Maven install required.

---

## Quick start

### 1. Clone the repository

```bash
git clone https://github.com/anjatasevska/codele.git
cd codele
```

### 2. Environment file

```powershell
# Windows
copy .env.example .env
```

```bash
# macOS / Linux
cp .env.example .env
```

Edit `.env` and set a strong `CODELE_ADMIN_PASSWORD`.

### 3. Start PostgreSQL

```powershell
.\scripts\setup.ps1
```

This runs `docker compose up -d` and waits for Postgres on port **5433** (default; port 5432 may be in use on your machine).

### 4. Start the application

```powershell
.\scripts\start.ps1
```

On first run:

- Flyway applies `V1__initial_schema.sql`
- Default **admin** user is created from `.env`

### 5. Open the app

| URL | Purpose |
|-----|---------|
| http://localhost:8080 | Home |
| http://localhost:8080/login | Login |
| http://localhost:8080/admin | Admin panel |
| http://localhost:8080/actuator/health | Health check |

**Default admin credentials** (from `.env`):

- Username: `admin` (or `CODELE_ADMIN_USERNAME`)
- Password: value of `CODELE_ADMIN_PASSWORD`

> **Important:** Registered accounts are **players** only. Use the built-in `admin` account to manage puzzles.

---

## Admin guide

1. Log in at `/login` with the **admin** account.
2. You are redirected to `/admin`.
3. Fill in **Add new puzzle**:
   - **Slug** — URL-friendly id (e.g. `bubble-sort`)
   - **Answer** — program name (e.g. `BubbleSort`)
   - **Language** — e.g. `Java`
   - **Code** — the full source snippet
   - **Scheduled date** — set to **today** for the daily puzzle
4. Click **Create puzzle**.
5. Verify in **Archive** and play from **Home**.

### Bulk scheduling

To schedule puzzles for the next N days:

1. Create puzzles first (note their IDs in the list).
2. In **Bulk schedule**, pick a start date and enter IDs comma-separated: `1, 2, 3`.
3. Puzzle 1 → start date, puzzle 2 → start date + 1 day, etc.

---

## Database

| Environment | Location |
|-------------|----------|
| **Local** | Docker volume `codele_pg_data`, host `localhost:5433`, database `codele` |
| **Production** | Hosted Postgres (e.g. [Neon](https://neon.tech)) |

### Inspect data (local)

```powershell
docker exec -it codele-postgres psql -U codele -d codele -c "\dt"
docker exec -it codele-postgres psql -U codele -d codele -c "SELECT id, answer, scheduled_date FROM puzzles;"
```

### Main tables

| Table | Contents |
|-------|----------|
| `puzzles` | Puzzle content and daily schedule |
| `puzzle_tags` | Tags per puzzle |
| `app_users` | Admin and registered players |
| `user_stats` | Aggregated player statistics |
| `puzzle_attempts` | Completed game records |
| `flyway_schema_history` | Migration history |

---

## Configuration

Main file: `src/main/resources/application.yml`

| Profile | Use case | Database |
|---------|----------|----------|
| `local` (default) | `.\scripts\start.ps1` | Docker PostgreSQL |
| `prod` | Railway / Neon | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| `test` | `mvn test` | In-memory H2 |

Key environment variables (`.env`):

| Variable | Description |
|----------|-------------|
| `POSTGRES_HOST` | DB host (default `localhost`) |
| `POSTGRES_PORT` | DB port (default `5433`) |
| `POSTGRES_DB` | Database name |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | DB credentials |
| `CODELE_ADMIN_USERNAME` | Admin username |
| `CODELE_ADMIN_PASSWORD` | Admin password (synced on local startup) |
| `SERVER_PORT` | App port (default `8080`) |

---

## API overview

### Public gameplay API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/game/{id}/start` | Start or resume session |
| `POST` | `/api/game/{id}/guess` | Guess a token |
| `POST` | `/api/game/{id}/hint` | Request a hint |
| `POST` | `/api/game/{id}/final` | Submit final answer |
| `POST` | `/api/game/{id}/giveup` | Reveal answer |
| `GET` | `/api/archive` | Puzzle metadata list |
| `GET` | `/api/me/stats` | Current user stats |

### Admin API (requires `ROLE_ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/puzzles` | List all puzzles |
| `POST` | `/api/admin/puzzles` | Create puzzle |
| `PUT` | `/api/admin/puzzles/{id}` | Update puzzle |
| `DELETE` | `/api/admin/puzzles/{id}` | Delete puzzle |
| `POST` | `/api/admin/puzzles/schedule` | Bulk schedule by date |

---

## Project structure

```text
codele/
├── docker-compose.yml          # Local PostgreSQL
├── scripts/
│   ├── setup.ps1               # Start database
│   └── start.ps1               # Start Spring Boot app
├── src/main/java/.../codeleapp/
│   ├── config/                 # Security, seeders, Flyway
│   ├── domain/                 # JPA entities
│   ├── repository/             # Spring Data repositories
│   ├── service/                # Business logic
│   └── web/                    # MVC + REST controllers
├── src/main/resources/
│   ├── application.yml         # Profiles and config
│   ├── db/migration/           # Flyway SQL
│   ├── templates/              # Thymeleaf HTML
│   └── static/                 # CSS, JavaScript
├── DEPLOY.md                   # Neon + Railway guide
└── README.md
```

---

