package mk.ukim.finki.codeleapp.service;

import java.time.LocalDate;

public class DailyPuzzleNotFoundException extends RuntimeException {
    public DailyPuzzleNotFoundException(LocalDate date) {
        super("No daily puzzle scheduled for " + date + ". An admin must assign one in the admin panel.");
    }
}
