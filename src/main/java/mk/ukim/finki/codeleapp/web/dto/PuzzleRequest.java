package mk.ukim.finki.codeleapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import mk.ukim.finki.codeleapp.domain.Category;
import mk.ukim.finki.codeleapp.domain.Difficulty;

public record PuzzleRequest(
    @NotBlank String slug,
    @NotBlank String answer,
    @NotBlank String language,
    @NotNull Category category,
    @NotNull Difficulty difficulty,
    @NotBlank String codeContent,
    String shortClue,
    String explanation,
    LocalDate scheduledDate,
    List<String> tags
) {}
