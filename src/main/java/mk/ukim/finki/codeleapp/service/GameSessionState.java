package mk.ukim.finki.codeleapp.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSessionState {
    private Long puzzleId;
    private Set<String> revealedTokens = new HashSet<>();
    private Set<String> revealedLiterals = new HashSet<>();
    private Set<String> wrongGuesses = new HashSet<>();
    private int hintsUsed;
    private int finalGuesses;
    private int guessedWords;
    private boolean solved;
    private boolean finished;
    private Instant startedAt = Instant.now();
}
