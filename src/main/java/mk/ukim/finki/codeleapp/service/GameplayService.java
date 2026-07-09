package mk.ukim.finki.codeleapp.service;

import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mk.ukim.finki.codeleapp.domain.HintType;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import mk.ukim.finki.codeleapp.repository.PuzzleRepository;
import org.springframework.stereotype.Service;

@Service
public class GameplayService {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");
    private static final Pattern IDENTIFIER_GUESS_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final String SESSION_KEY = "CODELE_GAME_SESSIONS";

    private final PuzzleRepository puzzleRepository;

    public GameplayService(PuzzleRepository puzzleRepository) {
        this.puzzleRepository = puzzleRepository;
    }

    public Optional<Puzzle> findDailyPuzzle() {
        return puzzleRepository.findByScheduledDate(LocalDate.now());
    }

    public Puzzle getDailyPuzzle() {
        return findDailyPuzzle()
            .orElseThrow(() -> new DailyPuzzleNotFoundException(LocalDate.now()));
    }

    public Puzzle getPuzzleById(Long id) {
        return puzzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Puzzle not found"));
    }

    public List<Puzzle> getArchive() {
        return puzzleRepository.findByScheduledDateBeforeOrderByScheduledDateDesc(LocalDate.now());
    }

    public Puzzle getPlayablePuzzleById(Long id) {
        Puzzle puzzle = getPuzzleById(id);
        LocalDate scheduled = puzzle.getScheduledDate();
        if (scheduled != null && scheduled.isAfter(LocalDate.now())) {
            throw new PuzzleNotYetAvailableException(scheduled);
        }
        return puzzle;
    }

    public GameView startOrGetGame(HttpSession session, Puzzle puzzle) {
        GameSessionState state = sessions(session).computeIfAbsent(puzzle.getId(), k -> {
            GameSessionState s = new GameSessionState();
            s.setPuzzleId(puzzle.getId());
            return s;
        });
        return toView(state, puzzle);
    }

    public GameView guessWord(HttpSession session, Puzzle puzzle, String guess) {
        GameSessionState state = requireState(session, puzzle.getId());
        if (guess == null || guess.trim().isEmpty() || state.isFinished()) {
            return toView(state, puzzle);
        }
        String trimmed = guess.trim();
        String token = normalize(trimmed);

        if (isIdentifierGuess(trimmed)) {
            Set<String> valid = extractTokens(puzzle.getCodeContent());
            if (valid.contains(token)) {
                if (state.getRevealedTokens().contains(token)) {
                    return toView(state, puzzle);
                }
                state.getRevealedTokens().add(token);
                state.setGuessedWords(state.getGuessedWords() + 1);
                return toView(state, puzzle);
            }
        } else if (puzzle.getCodeContent().contains(trimmed)) {
            if (state.getRevealedLiterals().contains(trimmed)) {
                return toView(state, puzzle);
            }
            state.getRevealedLiterals().add(trimmed);
            state.setGuessedWords(state.getGuessedWords() + 1);
            return toView(state, puzzle);
        }

        if (state.getWrongGuesses().contains(token)) {
            return toView(state, puzzle);
        }
        state.getWrongGuesses().add(token);
        state.setGuessedWords(state.getGuessedWords() + 1);
        return toView(state, puzzle);
    }

    public GameView applyHint(HttpSession session, Puzzle puzzle, HintType hintType) {
        GameSessionState state = requireState(session, puzzle.getId());
        if (state.isFinished()) {
            return toView(state, puzzle);
        }
        if (hintType == HintType.REVEAL_TOKEN) {
            Optional<String> hidden = extractTokenOccurrences(puzzle.getCodeContent()).stream()
                .map(this::normalize)
                .filter(t -> !state.getRevealedTokens().contains(t))
                .findFirst();
            if (hidden.isEmpty()) {
                return toView(state, puzzle);
            }
            state.getRevealedTokens().add(hidden.get());
        }
        state.setHintsUsed(state.getHintsUsed() + 1);
        return toView(state, puzzle);
    }

    public boolean submitFinalAnswer(HttpSession session, Puzzle puzzle, String answer) {
        GameSessionState state = requireState(session, puzzle.getId());
        if (state.isFinished()) {
            return state.isSolved();
        }
        state.setFinalGuesses(state.getFinalGuesses() + 1);
        boolean solved = puzzle.getAnswer().equalsIgnoreCase(answer == null ? "" : answer.trim());
        state.setSolved(solved);
        state.setFinished(true);
        return solved;
    }

    /** Ends the game without solving; returns true if this call newly ended the session (for stats). */
    public boolean giveUp(HttpSession session, Puzzle puzzle) {
        GameSessionState state = requireState(session, puzzle.getId());
        if (state.isFinished()) {
            return false;
        }
        state.setSolved(false);
        state.setFinished(true);
        return true;
    }

    public long elapsedSeconds(HttpSession session, Long puzzleId) {
        GameSessionState state = requireState(session, puzzleId);
        return Duration.between(state.getStartedAt(), java.time.Instant.now()).toSeconds();
    }

    public GameSessionState requireState(HttpSession session, Long puzzleId) {
        return Optional.ofNullable(sessions(session).get(puzzleId))
            .orElseThrow(() -> new IllegalStateException("No active game for puzzle"));
    }

    public int calculateScore(Puzzle puzzle, GameSessionState state) {
        int revealPenalty = calculateRevealPercent(puzzle.getCodeContent(), state.getRevealedTokens()) * 5;
        int wrongPenalty = state.getWrongGuesses().size() * 40;
        int hintPenalty = state.getHintsUsed() * 60;
        int finalGuessPenalty = Math.max(0, state.getFinalGuesses() - 1) * 50;
        return Math.max(0, 1500 - revealPenalty - wrongPenalty - hintPenalty - finalGuessPenalty);
    }

    public int calculateRevealPercent(String code, Set<String> revealedTokens) {
        List<String> tokenList = extractTokenOccurrences(code);
        if (tokenList.isEmpty()) {
            return 100;
        }
        long visible = tokenList.stream().filter(revealedTokens::contains).count();
        return (int) ((visible * 100.0) / tokenList.size());
    }

    private GameView toView(GameSessionState state, Puzzle puzzle) {
        int revealPercent = calculateRevealPercent(puzzle.getCodeContent(), state.getRevealedTokens());
        Set<String> correctGuesses = new HashSet<>(state.getRevealedTokens());
        correctGuesses.addAll(state.getRevealedLiterals());
        return GameView.builder()
            .puzzleId(puzzle.getId())
            .code(state.isFinished()
                ? puzzle.getCodeContent()
                : maskCode(puzzle.getCodeContent(), state.getRevealedTokens(), state.getRevealedLiterals()))
            .language(puzzle.getLanguage())
            .revealPercent(revealPercent)
            .hintsUsed(state.getHintsUsed())
            .wrongGuessCount(state.getWrongGuesses().size())
            .guessedWords(state.getGuessedWords())
            .canFinalGuess(revealPercent >= 35 || state.getHintsUsed() >= 2)
            .solved(state.isSolved())
            .finished(state.isFinished())
            .wrongGuesses(state.getWrongGuesses())
            .correctGuesses(correctGuesses)
            .build();
    }

    private String maskCode(String code, Set<String> revealedTokens, Set<String> revealedLiterals) {
        boolean[] visible = new boolean[code.length()];
        for (int i = 0; i < code.length(); i++) {
            if (Character.isWhitespace(code.charAt(i))) {
                visible[i] = true;
            }
        }

        Matcher matcher = TOKEN_PATTERN.matcher(code);
        while (matcher.find()) {
            String token = normalize(matcher.group());
            if (revealedTokens.contains(token)) {
                for (int i = matcher.start(); i < matcher.end(); i++) {
                    visible[i] = true;
                }
            }
        }

        for (String literal : revealedLiterals) {
            int idx = 0;
            while ((idx = code.indexOf(literal, idx)) >= 0) {
                for (int i = idx; i < idx + literal.length(); i++) {
                    visible[i] = true;
                }
                idx += Math.max(1, literal.length());
            }
        }

        StringBuilder sb = new StringBuilder(code.length());
        for (int i = 0; i < code.length(); i++) {
            sb.append(visible[i] ? code.charAt(i) : '~');
        }
        return sb.toString();
    }

    private boolean isIdentifierGuess(String guess) {
        return IDENTIFIER_GUESS_PATTERN.matcher(guess).matches();
    }

    private Set<String> extractTokens(String code) {
        return new HashSet<>(extractTokenOccurrences(code));
    }

    private List<String> extractTokenOccurrences(String code) {
        Matcher matcher = TOKEN_PATTERN.matcher(code);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(normalize(matcher.group()));
        }
        return tokens;
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, GameSessionState> sessions(HttpSession session) {
        Object value = session.getAttribute(SESSION_KEY);
        if (value == null) {
            Map<Long, GameSessionState> map = new HashMap<>();
            session.setAttribute(SESSION_KEY, map);
            return map;
        }
        return (Map<Long, GameSessionState>) value;
    }
}
