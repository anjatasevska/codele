package mk.ukim.finki.codeleapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import mk.ukim.finki.codeleapp.repository.PuzzleRepository;
import mk.ukim.finki.codeleapp.web.dto.BulkScheduleRequest;
import mk.ukim.finki.codeleapp.web.dto.PuzzleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PuzzleAdminService {
    private final PuzzleRepository puzzleRepository;

    public PuzzleAdminService(PuzzleRepository puzzleRepository) {
        this.puzzleRepository = puzzleRepository;
    }

    public List<Puzzle> findAll() {
        return puzzleRepository.findAll().stream()
            .sorted((a, b) -> {
                if (a.getScheduledDate() != null && b.getScheduledDate() != null) {
                    return a.getScheduledDate().compareTo(b.getScheduledDate());
                }
                if (a.getScheduledDate() != null) {
                    return -1;
                }
                if (b.getScheduledDate() != null) {
                    return 1;
                }
                return Long.compare(a.getId(), b.getId());
            })
            .toList();
    }

    public List<Puzzle> findScheduled() {
        return puzzleRepository.findByScheduledDateIsNotNullOrderByScheduledDateAsc();
    }

    public Puzzle findById(Long id) {
        return puzzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Puzzle not found"));
    }

    @Transactional
    public Puzzle create(PuzzleRequest request) {
        validateSlugUnique(request.slug(), null);
        validateScheduledDate(request.scheduledDate(), null);
        Puzzle puzzle = new Puzzle();
        applyRequest(puzzle, request);
        return puzzleRepository.save(puzzle);
    }

    @Transactional
    public Puzzle update(Long id, PuzzleRequest request) {
        Puzzle puzzle = findById(id);
        validateSlugUnique(request.slug(), id);
        validateScheduledDate(request.scheduledDate(), id);
        applyRequest(puzzle, request);
        return puzzleRepository.save(puzzle);
    }

    @Transactional
    public void delete(Long id) {
        if (!puzzleRepository.existsById(id)) {
            throw new IllegalArgumentException("Puzzle not found");
        }
        puzzleRepository.deleteById(id);
    }

    @Transactional
    public List<Puzzle> bulkSchedule(BulkScheduleRequest request) {
        LocalDate date = request.startDate();
        List<Puzzle> updated = new ArrayList<>();
        for (Long puzzleId : request.puzzleIds()) {
            Puzzle puzzle = findById(puzzleId);
            if (puzzleRepository.existsByScheduledDateAndIdNot(date, puzzleId)) {
                throw new IllegalArgumentException(
                    "Date " + date + " is already assigned to another puzzle.");
            }
            puzzle.setScheduledDate(date);
            updated.add(puzzleRepository.save(puzzle));
            date = date.plusDays(1);
        }
        return updated;
    }

    @Transactional
    public Puzzle clearSchedule(Long id) {
        Puzzle puzzle = findById(id);
        puzzle.setScheduledDate(null);
        return puzzleRepository.save(puzzle);
    }

    private void applyRequest(Puzzle puzzle, PuzzleRequest request) {
        puzzle.setSlug(normalizeSlug(request.slug()));
        puzzle.setAnswer(request.answer().trim());
        puzzle.setLanguage(request.language().trim());
        puzzle.setCategory(request.category());
        puzzle.setDifficulty(request.difficulty());
        puzzle.setCodeContent(request.codeContent());
        puzzle.setShortClue(request.shortClue());
        puzzle.setExplanation(request.explanation());
        puzzle.setScheduledDate(request.scheduledDate());
        puzzle.setTags(tagsFrom(request.tags()));
    }

    private Set<String> tagsFrom(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> result = new HashSet<>();
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                result.add(tag.trim().toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    private void validateSlugUnique(String slug, Long excludeId) {
        String normalized = normalizeSlug(slug);
        puzzleRepository.findBySlug(normalized).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new IllegalArgumentException("Slug already in use: " + normalized);
            }
        });
    }

    private void validateScheduledDate(LocalDate scheduledDate, Long excludeId) {
        if (scheduledDate == null) {
            return;
        }
        boolean taken = excludeId == null
            ? puzzleRepository.existsByScheduledDate(scheduledDate)
            : puzzleRepository.existsByScheduledDateAndIdNot(scheduledDate, excludeId);
        if (taken) {
            throw new IllegalArgumentException("Another puzzle is already scheduled for " + scheduledDate);
        }
    }
}
