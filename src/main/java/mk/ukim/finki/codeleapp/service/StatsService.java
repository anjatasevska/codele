package mk.ukim.finki.codeleapp.service;

import java.util.List;
import java.util.Map;
import mk.ukim.finki.codeleapp.domain.AppUser;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import mk.ukim.finki.codeleapp.domain.PuzzleAttempt;
import mk.ukim.finki.codeleapp.domain.UserStats;
import mk.ukim.finki.codeleapp.repository.PuzzleAttemptRepository;
import mk.ukim.finki.codeleapp.repository.UserStatsRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private final UserStatsRepository statsRepository;
    private final PuzzleAttemptRepository attemptRepository;

    public StatsService(UserStatsRepository statsRepository, PuzzleAttemptRepository attemptRepository) {
        this.statsRepository = statsRepository;
        this.attemptRepository = attemptRepository;
    }

    public void recordAttempt(AppUser user, Puzzle puzzle, GameSessionState state, int revealPercent, int score, long durationSeconds) {
        PuzzleAttempt attempt = new PuzzleAttempt();
        attempt.setUser(user);
        attempt.setPuzzle(puzzle);
        attempt.setSolved(state.isSolved());
        attempt.setRevealPercent(revealPercent);
        attempt.setWrongGuesses(state.getWrongGuesses().size());
        attempt.setWordGuesses(state.getGuessedWords());
        attempt.setHintsUsed(state.getHintsUsed());
        attempt.setScore(score);
        attempt.setDurationSeconds(durationSeconds);
        attemptRepository.save(attempt);

        if (user == null) {
            return;
        }
        UserStats stats = statsRepository.findByUser(user).orElseGet(() -> {
            UserStats s = new UserStats();
            s.setUser(user);
            return s;
        });

        stats.setPuzzlesPlayed(stats.getPuzzlesPlayed() + 1);
        stats.setTotalWrongGuesses(stats.getTotalWrongGuesses() + state.getWrongGuesses().size());
        stats.setTotalWordGuesses(stats.getTotalWordGuesses() + state.getGuessedWords());
        stats.setHintsUsed(stats.getHintsUsed() + state.getHintsUsed());
        stats.setTotalSolveSeconds(stats.getTotalSolveSeconds() + durationSeconds);

        if (state.isSolved()) {
            stats.setPuzzlesSolved(stats.getPuzzlesSolved() + 1);
            stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            stats.setBestStreak(Math.max(stats.getBestStreak(), stats.getCurrentStreak()));
            stats.setTotalRevealPercentOnSolve(stats.getTotalRevealPercentOnSolve() + revealPercent);
            if (revealPercent <= 25) stats.setSolvedUnder25(stats.getSolvedUnder25() + 1);
            else if (revealPercent <= 50) stats.setSolvedUnder50(stats.getSolvedUnder50() + 1);
            else if (revealPercent <= 75) stats.setSolvedUnder75(stats.getSolvedUnder75() + 1);
            else stats.setSolvedOver75(stats.getSolvedOver75() + 1);
            if (revealPercent <= 20 && state.getWrongGuesses().isEmpty() && state.getHintsUsed() == 0) {
                stats.setPerfectSolves(stats.getPerfectSolves() + 1);
            }
        } else {
            stats.setCurrentStreak(0);
        }
        statsRepository.save(stats);
    }

    public Map<String, Object> statsDto(UserStats stats) {
        double solveRate = stats.getPuzzlesPlayed() == 0 ? 0 : (stats.getPuzzlesSolved() * 100.0 / stats.getPuzzlesPlayed());
        double avgRevealOnSolve = stats.getPuzzlesSolved() == 0 ? 0 : stats.getTotalRevealPercentOnSolve() / stats.getPuzzlesSolved();
        double avgWrong = stats.getPuzzlesPlayed() == 0 ? 0 : (double) stats.getTotalWrongGuesses() / stats.getPuzzlesPlayed();
        double avgWords = stats.getPuzzlesPlayed() == 0 ? 0 : (double) stats.getTotalWordGuesses() / stats.getPuzzlesPlayed();

        return Map.ofEntries(
            Map.entry("puzzlesPlayed", stats.getPuzzlesPlayed()),
            Map.entry("puzzlesSolved", stats.getPuzzlesSolved()),
            Map.entry("solveRate", round2(solveRate)),
            Map.entry("currentStreak", stats.getCurrentStreak()),
            Map.entry("bestStreak", stats.getBestStreak()),
            Map.entry("averageRevealWhenSolved", round2(avgRevealOnSolve)),
            Map.entry("perfectSolves", stats.getPerfectSolves()),
            Map.entry("solvedUnder25", stats.getSolvedUnder25()),
            Map.entry("solvedUnder50", stats.getSolvedUnder50()),
            Map.entry("solvedUnder75", stats.getSolvedUnder75()),
            Map.entry("solvedOver75", stats.getSolvedOver75()),
            Map.entry("averageWrongGuesses", round2(avgWrong)),
            Map.entry("averageWordGuesses", round2(avgWords)),
            Map.entry("hintsUsed", stats.getHintsUsed())
        );
    }

    public List<PuzzleAttempt> leaderboard() {
        return attemptRepository.findTop10BySolvedTrueOrderByScoreDescCreatedAtAsc();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
