package mk.ukim.finki.codeleapp.service;

import java.time.LocalDate;

public class PuzzleNotYetAvailableException extends RuntimeException {
    public PuzzleNotYetAvailableException(LocalDate scheduledDate) {
        super("This puzzle is scheduled for " + scheduledDate + " and is not available yet.");
    }
}
