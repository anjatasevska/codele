package mk.ukim.finki.codeleapp.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mk.ukim.finki.codeleapp.domain.Category;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {
    Optional<Puzzle> findBySlug(String slug);

    Optional<Puzzle> findByScheduledDate(LocalDate date);

    List<Puzzle> findByCategory(Category category);

    List<Puzzle> findByScheduledDateIsNotNullOrderByScheduledDateAsc();

    List<Puzzle> findByScheduledDateBetweenOrderByScheduledDateAsc(LocalDate start, LocalDate end);

    List<Puzzle> findByScheduledDateBeforeOrderByScheduledDateDesc(LocalDate date);

    boolean existsByScheduledDateAndIdNot(LocalDate scheduledDate, Long id);

    boolean existsByScheduledDate(LocalDate scheduledDate);
}
