package mk.ukim.finki.codeleapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import mk.ukim.finki.codeleapp.repository.PuzzleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

@ExtendWith(MockitoExtension.class)
class GameplayServiceTest {

    @Mock
    private PuzzleRepository puzzleRepository;

    private GameplayService gameplayService;
    private HttpSession session;
    private Puzzle puzzle;

    @BeforeEach
    void setUp() {
        gameplayService = new GameplayService(puzzleRepository);
        session = new MockHttpSession();
        puzzle = new Puzzle();
        puzzle.setId(1L);
        puzzle.setLanguage("Java");
        puzzle.setCodeContent("""
            for (int i = 0; i < arr.length - 1; i++) {
            }
            """);
    }

    @Test
    void guessingTokenRevealsOnlyThatToken_notOperatorsOrLiterals() {
        gameplayService.startOrGetGame(session, puzzle);

        GameView view = gameplayService.guessWord(session, puzzle, "i");

        assertThat(view.getCode()).contains("i");
        assertThat(view.getCode()).doesNotContain("=");
        assertThat(view.getCode()).doesNotContain("<");
        assertThat(view.getCode()).doesNotContain("0");
        assertThat(view.getCode()).doesNotContain("1");
        assertThat(view.getCode()).doesNotContain("(");
        assertThat(view.getCode()).doesNotContain(")");
        assertThat(view.getCode()).doesNotContain(";");
    }

    @Test
    void guessingSymbolRevealsEveryOccurrenceInCode() {
        gameplayService.startOrGetGame(session, puzzle);

        GameView view = gameplayService.guessWord(session, puzzle, "=");

        assertThat(view.getCorrectGuesses()).contains("=");
        assertThat(view.getWrongGuesses()).doesNotContain("=");
        assertThat(view.getCode()).contains("=");
    }

    @Test
    void archiveListsOnlyPastScheduledPuzzles() {
        LocalDate today = LocalDate.now();
        Puzzle past = new Puzzle();
        past.setId(1L);
        past.setScheduledDate(today.minusDays(1));
        Puzzle future = new Puzzle();
        future.setId(2L);
        future.setScheduledDate(today.plusDays(1));

        when(puzzleRepository.findByScheduledDateBeforeOrderByScheduledDateDesc(today))
            .thenReturn(java.util.List.of(past));
        when(puzzleRepository.findById(2L)).thenReturn(java.util.Optional.of(future));

        assertThat(gameplayService.getArchive()).containsExactly(past);
        assertThatThrownBy(() -> gameplayService.getPlayablePuzzleById(future.getId()))
            .isInstanceOf(PuzzleNotYetAvailableException.class);
    }
}
