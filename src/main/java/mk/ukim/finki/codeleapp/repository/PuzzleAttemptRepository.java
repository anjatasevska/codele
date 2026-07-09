package mk.ukim.finki.codeleapp.repository;

import java.util.List;
import mk.ukim.finki.codeleapp.domain.AppUser;
import mk.ukim.finki.codeleapp.domain.PuzzleAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleAttemptRepository extends JpaRepository<PuzzleAttempt, Long> {
    List<PuzzleAttempt> findTop10BySolvedTrueOrderByScoreDescCreatedAtAsc();
    List<PuzzleAttempt> findByUser(AppUser user);
}
