# Codele - Project Blueprint

## Name options
- Codele
- TokenTrace
- GuessTheCode
- CipherCode
- CodeWhisper

Selected: **Codele**

## Modern stack decision
- Current implementation in this repo: **Spring Boot + Thymeleaf + JPA + Spring Security + H2/PostgreSQL-compatible config**
- Why: repository is already Spring Boot, and local environment currently lacks Node tooling.
- Scale-ready path: migrate frontend to Next.js later while keeping REST API contracts stable.

## Database schema
- `app_users(id, username, email, password_hash)`
- `user_stats(id, user_id, puzzles_played, puzzles_solved, solve_rate derived, current_streak, best_streak, avg_reveal derived, perfect_solves, solved_under_25, solved_under_50, solved_under_75, solved_over_75, total_wrong_guesses, total_word_guesses, hints_used, total_solve_seconds)`
- `puzzles(id, slug, answer, language, category, difficulty, code_content, short_clue, explanation, scheduled_date)`
- `puzzle_tags(puzzle_id, tag)`
- `puzzle_attempts(id, puzzle_id, user_id nullable for guest, solved, reveal_percent, wrong_guesses, word_guesses, hints_used, score, duration_seconds, created_at)`

## API design
- `POST /api/game/{id}/start`
- `POST /api/game/{id}/guess` body `{ guess }`
- `POST /api/game/{id}/hint` body `{ type }`
- `POST /api/game/{id}/final` body `{ answer }`
- `GET /api/archive`
- `GET /api/me/stats`

## Puzzle logic
- Token regex: words/identifiers only (`[A-Za-z_][A-Za-z0-9_]*`)
- Preserve punctuation/indent/line breaks.
- Hidden tokens rendered as fixed placeholder chars.
- Correct token guess reveals all occurrences.
- Wrong guesses tracked separately.
- Final answer unlocks at reveal threshold or after hints.

## Scoring
- Base 1500
- Reveal penalty: `revealPercent * 5`
- Wrong guess penalty: `wrongGuesses * 40`
- Hint penalty: `hintsUsed * 60`
- Extra final guess penalty: `50` each after first final guess
- Floor score at 0

## Auth flow
- Guests can fully play.
- Registration via `/register`, login via `/login`.
- Logged-in attempts update `user_stats`.

## MVP features delivered
- Daily puzzle mode (scheduled date)
- Archive/free play mode
- Guess token reveal loop
- Hint system (language/category/description/reveal token)
- Final answer submission and summary
- Persistent stats for logged-in users
- Leaderboard from solved attempts
- Admin content overview page
- Seeded puzzles: BubbleSort, BinarySearch, FizzBuzz, PalindromeChecker, TwoSum, StackImplementation

## Stretch roadmap
- Next.js frontend + mobile-first animations
- Rich syntax highlighting with token-level spans
- Puzzle CRUD admin with role-based auth
- Anti-cheat and signed puzzle payloads
- Global and friends leaderboards
- Achievements badges and seasonal events
