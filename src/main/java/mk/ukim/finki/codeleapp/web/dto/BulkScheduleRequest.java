package mk.ukim.finki.codeleapp.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BulkScheduleRequest(
    @NotNull LocalDate startDate,
    @NotEmpty List<Long> puzzleIds
) {}
