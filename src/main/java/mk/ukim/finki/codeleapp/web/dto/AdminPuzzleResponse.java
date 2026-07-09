package mk.ukim.finki.codeleapp.web.dto;

import java.time.LocalDate;
import java.util.Set;
import mk.ukim.finki.codeleapp.domain.Category;
import mk.ukim.finki.codeleapp.domain.Difficulty;
import mk.ukim.finki.codeleapp.domain.Puzzle;

public record AdminPuzzleResponse(
    Long id,
    String slug,
    String answer,
    String language,
    Category category,
    Difficulty difficulty,
    String codeContent,
    String shortClue,
    String explanation,
    LocalDate scheduledDate,
    Set<String> tags
) {
    public static AdminPuzzleResponse from(Puzzle puzzle) {
        return new AdminPuzzleResponse(
            puzzle.getId(),
            puzzle.getSlug(),
            puzzle.getAnswer(),
            puzzle.getLanguage(),
            puzzle.getCategory(),
            puzzle.getDifficulty(),
            puzzle.getCodeContent(),
            puzzle.getShortClue(),
            puzzle.getExplanation(),
            puzzle.getScheduledDate(),
            puzzle.getTags()
        );
    }
}
