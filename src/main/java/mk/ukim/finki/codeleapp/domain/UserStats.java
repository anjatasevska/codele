package mk.ukim.finki.codeleapp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_stats")
public class UserStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private AppUser user;

    private int puzzlesPlayed;
    private int puzzlesSolved;
    private int currentStreak;
    private int bestStreak;
    private int perfectSolves;

    private int solvedUnder25;
    private int solvedUnder50;
    private int solvedUnder75;
    private int solvedOver75;

    private int totalWrongGuesses;
    private int totalWordGuesses;
    private double totalRevealPercentOnSolve;
    private int hintsUsed;
    private long totalSolveSeconds;
}
