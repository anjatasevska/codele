package mk.ukim.finki.codeleapp.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import mk.ukim.finki.codeleapp.service.PuzzleAdminService;
import mk.ukim.finki.codeleapp.web.dto.AdminPuzzleResponse;
import mk.ukim.finki.codeleapp.web.dto.BulkScheduleRequest;
import mk.ukim.finki.codeleapp.web.dto.PuzzleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/puzzles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPuzzleController {
    private final PuzzleAdminService puzzleAdminService;

    public AdminPuzzleController(PuzzleAdminService puzzleAdminService) {
        this.puzzleAdminService = puzzleAdminService;
    }

    @GetMapping
    public List<AdminPuzzleResponse> list() {
        return puzzleAdminService.findAll().stream().map(AdminPuzzleResponse::from).toList();
    }

    @GetMapping("/scheduled")
    public List<AdminPuzzleResponse> scheduled() {
        return puzzleAdminService.findScheduled().stream().map(AdminPuzzleResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AdminPuzzleResponse get(@PathVariable Long id) {
        return AdminPuzzleResponse.from(puzzleAdminService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminPuzzleResponse create(@Valid @RequestBody PuzzleRequest request) {
        return AdminPuzzleResponse.from(puzzleAdminService.create(request));
    }

    @PutMapping("/{id}")
    public AdminPuzzleResponse update(@PathVariable Long id, @Valid @RequestBody PuzzleRequest request) {
        return AdminPuzzleResponse.from(puzzleAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        puzzleAdminService.delete(id);
    }

    @PostMapping("/schedule")
    public List<AdminPuzzleResponse> bulkSchedule(@Valid @RequestBody BulkScheduleRequest request) {
        return puzzleAdminService.bulkSchedule(request).stream().map(AdminPuzzleResponse::from).toList();
    }

    @PostMapping("/{id}/unschedule")
    public AdminPuzzleResponse unschedule(@PathVariable Long id) {
        return AdminPuzzleResponse.from(puzzleAdminService.clearSchedule(id));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
