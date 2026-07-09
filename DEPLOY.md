# Deploy Codele (Neon + Railway)

Production uses the **`prod`** profile: Flyway migrations + PostgreSQL on Neon + Spring Boot on Railway.

## 1. Neon (database)

1. [neon.tech](https://neon.tech) → **New project** → region near users.
2. **Connect** → copy host, database, user, password.
3. JDBC URL:

   ```text
   jdbc:postgresql://YOUR-HOST/neondb?sslmode=require
   ```

Tables are created automatically on first app start via **Flyway** (`V1__initial_schema.sql`).

## 2. GitHub

Push the repository (do not commit `.env`).

## 3. Railway (application)

1. [railway.app](https://railway.app) → **Deploy from GitHub**.
2. **Variables:**

   | Variable | Value |
   |----------|--------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | Neon JDBC URL |
   | `DB_USERNAME` | Neon user |
   | `DB_PASSWORD` | Neon password |
   | `DB_DRIVER` | `org.postgresql.Driver` |
   | `CODELE_ADMIN_USERNAME` | `admin` |
   | `CODELE_ADMIN_EMAIL` | your email |
   | `CODELE_ADMIN_PASSWORD` | strong password |

3. **Generate domain** → deploy.

4. `https://YOUR-APP.up.railway.app/actuator/health` should return `{"status":"UP"}`.

5. Log in → `/admin` → create and schedule puzzles.

## Local vs production

| | Local | Production |
|--|--------|------------|
| Profile | `local` | `prod` |
| DB | Docker (`.\scripts\setup.ps1`) | Neon |
| App | `.\scripts\start.ps1` | Railway |
| Schema | Flyway (same SQL files) | Flyway (same SQL files) |
