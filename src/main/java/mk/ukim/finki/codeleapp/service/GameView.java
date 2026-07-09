package mk.ukim.finki.codeleapp.service;

import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GameView {
    Long puzzleId;
    String code;
    String language;
    int revealPercent;
    int hintsUsed;
    int wrongGuessCount;
    int guessedWords;
    boolean canFinalGuess;
    boolean solved;
    boolean finished;
    Set<String> wrongGuesses;
    Set<String> correctGuesses;
}
