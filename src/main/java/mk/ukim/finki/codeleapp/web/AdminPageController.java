package mk.ukim.finki.codeleapp.web;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import mk.ukim.finki.codeleapp.domain.Category;
import mk.ukim.finki.codeleapp.domain.Difficulty;
import mk.ukim.finki.codeleapp.domain.Puzzle;
import mk.ukim.finki.codeleapp.service.PuzzleAdminService;
import mk.ukim.finki.codeleapp.web.dto.BulkScheduleRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminPageController {
    private final PuzzleAdminService puzzleAdminService;

    public AdminPageController(PuzzleAdminService puzzleAdminService) {
        this.puzzleAdminService = puzzleAdminService;
    }

    @GetMapping
    public String adminPanel(
        Model model,
        @RequestParam(required = false) Long editId
    ) {
        model.addAttribute("puzzles", puzzleAdminService.findAll());
        model.addAttribute("categories", Category.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("today", LocalDate.now());

        PuzzleForm form = new PuzzleForm();
        if (editId == null) {
            form.setScheduledDate(LocalDate.now());
        }
        if (editId != null) {
            Puzzle puzzle = puzzleAdminService.findById(editId);
            form.setSlug(puzzle.getSlug());
            form.setAnswer(puzzle.getAnswer());
            form.setLanguage(puzzle.getLanguage());
            form.setCategory(puzzle.getCategory());
            form.setDifficulty(puzzle.getDifficulty());
            form.setCodeContent(puzzle.getCodeContent());
            form.setShortClue(puzzle.getShortClue());
            form.setExplanation(puzzle.getExplanation());
            form.setScheduledDate(puzzle.getScheduledDate());
            if (puzzle.getTags() != null && !puzzle.getTags().isEmpty()) {
                form.setTags(String.join(", ", puzzle.getTags()));
            }
            model.addAttribute("editId", editId);
        }
        model.addAttribute("puzzleForm", form);
        return "admin";
    }

    @PostMapping("/puzzles")
    public String createPuzzle(
        @ModelAttribute PuzzleForm puzzleForm,
        RedirectAttributes redirectAttributes
    ) {
        return savePuzzle(null, puzzleForm, redirectAttributes);
    }

    @PostMapping("/puzzles/{id}")
    public String updatePuzzle(
        @PathVariable Long id,
        @ModelAttribute PuzzleForm puzzleForm,
        RedirectAttributes redirectAttributes
    ) {
        return savePuzzle(id, puzzleForm, redirectAttributes);
    }

    @PostMapping("/puzzles/{id}/delete")
    public String deletePuzzle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            puzzleAdminService.delete(id);
            redirectAttributes.addFlashAttribute("message", "Puzzle deleted.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/schedule")
    public String bulkSchedule(
        @RequestParam LocalDate startDate,
        @RequestParam String puzzleIds,
        RedirectAttributes redirectAttributes
    ) {
        try {
            List<Long> ids = Arrays.stream(puzzleIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
            List<Puzzle> updated = puzzleAdminService.bulkSchedule(new BulkScheduleRequest(startDate, ids));
            redirectAttributes.addFlashAttribute("message", "Scheduled " + updated.size() + " puzzle(s) starting " + startDate + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    private String savePuzzle(Long id, PuzzleForm puzzleForm, RedirectAttributes redirectAttributes) {
        try {
            if (id == null) {
                puzzleAdminService.create(puzzleForm.toRequest());
                redirectAttributes.addFlashAttribute("message", "Puzzle created! It now appears in the archive.");
            } else {
                puzzleAdminService.update(id, puzzleForm.toRequest());
                redirectAttributes.addFlashAttribute("message", "Puzzle updated.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("puzzleForm", puzzleForm);
            if (id != null) {
                return "redirect:/admin?editId=" + id;
            }
            return "redirect:/admin";
        }
        return "redirect:/admin";
    }
}
