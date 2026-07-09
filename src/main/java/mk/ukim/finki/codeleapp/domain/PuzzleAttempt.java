package mk.ukim.finki.codeleapp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "puzzle_attempts")
public class PuzzleAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Puzzle puzzle;

    @ManyToOne
    private AppUser user;

    private boolean solved;
    private int revealPercent;
    private int wrongGuesses;
    private int wordGuesses;
    private int hintsUsed;
    private int score;
    private long durationSeconds;
    private LocalDateTime createdAt = LocalDateTime.now();
}
