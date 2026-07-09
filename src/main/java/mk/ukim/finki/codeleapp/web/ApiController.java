package mk.ukim.finki.codeleapp.web;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mk.ukim.finki.codeleapp.domain.AppUser;
import mk.ukim.finki.codeleapp.domain.HintType;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import mk.ukim.finki.codeleapp.repository.AppUserRepository;
import mk.ukim.finki.codeleapp.repository.UserStatsRepository;
import mk.ukim.finki.codeleapp.service.GameSessionState;
import mk.ukim.finki.codeleapp.service.GameView;
import mk.ukim.finki.codeleapp.service.GameplayService;
import mk.ukim.finki.codeleapp.service.StatsService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class ApiController {
    private final GameplayService gameplayService;
    private final StatsService statsService;
    private final AppUserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    public ApiController(GameplayService gameplayService, StatsService statsService, AppUserRepository userRepository, UserStatsRepository userStatsRepository) {
        this.gameplayService = gameplayService;
        this.statsService = statsService;
        this.userRepository = userRepository;
        this.userStatsRepository = userStatsRepository;
    }

    @PostMapping("/game/{id}/start")
    public GameView start(@PathVariable Long id, HttpSession session) {
        Puzzle puzzle = gameplayService.getPlayablePuzzleById(id);
        return gameplayService.startOrGetGame(session, puzzle);
    }

    @PostMapping("/game/{id}/guess")
    public GameView guess(@PathVariable Long id, @RequestBody GuessRequest request, HttpSession session) {
        Puzzle puzzle = gameplayService.getPlayablePuzzleById(id);
        return gameplayService.guessWord(session, puzzle, request.guess());
    }

    @PostMapping("/game/{id}/hint")
    public GameView hint(@PathVariable Long id, @RequestBody HintRequest request, HttpSession session) {
        Puzzle puzzle = gameplayService.getPlayablePuzzleById(id);
        return gameplayService.applyHint(session, puzzle, request.type());
    }

    @PostMapping("/game/{id}/final")
    public Map<String, Object> finalGuess(@PathVariable Long id, @RequestBody FinalRequest request, HttpSession session, Principal principal) {
        Puzzle puzzle = gameplayService.getPlayablePuzzleById(id);
        GameSessionState state = gameplayService.requireState(session, puzzle.getId());
        boolean wasFinished = state.isFinished();
        boolean solved = gameplayService.submitFinalAnswer(session, puzzle, request.answer());
        state = gameplayService.requireState(session, puzzle.getId());
        int revealPercent = gameplayService.calculateRevealPercent(puzzle.getCodeContent(), state.getRevealedTokens());
        int score = gameplayService.calculateScore(puzzle, state);
        long duration = gameplayService.elapsedSeconds(session, puzzle.getId());

        AppUser user = principal == null ? null : userRepository.findByUsername(principal.getName()).orElse(null);
        if (!wasFinished) {
            statsService.recordAttempt(user, puzzle, state, revealPercent, score, duration);
        }

        return Map.of(
            "solved", solved,
            "score", score,
            "revealPercent", revealPercent,
            "wrongGuesses", state.getWrongGuesses().size(),
            "guessedWords", state.getGuessedWords(),
            "explanation", puzzle.getExplanation() == null ? "" : puzzle.getExplanation(),
            "correctAnswer", puzzle.getAnswer()
        );
    }

    @PostMapping("/game/{id}/giveup")
    public Map<String, Object> giveUp(@PathVariable Long id, HttpSession session, Principal principal) {
        Puzzle puzzle = gameplayService.getPlayablePuzzleById(id);
        boolean newlyFinished = gameplayService.giveUp(session, puzzle);
        GameSessionState state = gameplayService.requireState(session, puzzle.getId());
        int revealPercent = gameplayService.calculateRevealPercent(puzzle.getCodeContent(), state.getRevealedTokens());
        int score = 0;
        long duration = gameplayService.elapsedSeconds(session, puzzle.getId());

        AppUser user = principal == null ? null : userRepository.findByUsername(principal.getName()).orElse(null);
        if (newlyFinished) {
            statsService.recordAttempt(user, puzzle, state, revealPercent, score, duration);
        }

        return Map.of(
            "solved", false,
            "score", score,
            "revealPercent", revealPercent,
            "wrongGuesses", state.getWrongGuesses().size(),
            "guessedWords", state.getGuessedWords(),
            "explanation", puzzle.getExplanation() == null ? "" : puzzle.getExplanation(),
            "correctAnswer", puzzle.getAnswer()
        );
    }

    @GetMapping("/archive")
    public List<Map<String, Object>> archive() {
        return gameplayService.getArchive().stream().map(p -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", p.getId());
            row.put("language", p.getLanguage());
            if (p.getScheduledDate() != null) {
                row.put("scheduledDate", p.getScheduledDate().toString());
            }
            return row;
        }).toList();
    }

    @GetMapping("/me/stats")
    public Map<String, Object> myStats(Principal principal) {
        if (principal == null) {
            return Map.of("guest", true);
        }
        return userRepository.findByUsername(principal.getName())
            .flatMap(userStatsRepository::findByUser)
            .map(statsService::statsDto)
            .orElse(Map.of("guest", false, "empty", true));
    }

    public record GuessRequest(@NotBlank String guess) {}
    public record FinalRequest(@NotBlank String answer) {}
    public record HintRequest(HintType type) {}
}
