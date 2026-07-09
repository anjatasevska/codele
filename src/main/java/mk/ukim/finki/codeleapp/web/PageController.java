package mk.ukim.finki.codeleapp.web;

import java.security.Principal;
import mk.ukim.finki.codeleapp.repository.AppUserRepository;
import mk.ukim.finki.codeleapp.repository.UserStatsRepository;
import mk.ukim.finki.codeleapp.service.DailyPuzzleNotFoundException;
import mk.ukim.finki.codeleapp.service.GameplayService;
import mk.ukim.finki.codeleapp.service.PuzzleNotYetAvailableException;
import mk.ukim.finki.codeleapp.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    private final GameplayService gameplayService;
    private final StatsService statsService;
    private final AppUserRepository userRepository;
    private final UserStatsRepository statsRepository;

    public PageController(
        GameplayService gameplayService,
        StatsService statsService,
        AppUserRepository userRepository,
        UserStatsRepository statsRepository
    ) {
        this.gameplayService = gameplayService;
        this.statsService = statsService;
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        gameplayService.findDailyPuzzle().ifPresentOrElse(
            puzzle -> model.addAttribute("dailyPuzzle", puzzle),
            () -> model.addAttribute("noDailyPuzzle", true)
        );
        return "home";
    }

    @GetMapping("/how-to-play")
    public String howToPlay() {
        return "how-to-play";
    }

    @GetMapping("/play/daily")
    public String playDaily(Model model) {
        try {
            model.addAttribute("puzzle", gameplayService.getDailyPuzzle());
            return "play";
        } catch (DailyPuzzleNotFoundException ex) {
            model.addAttribute("message", ex.getMessage());
            return "no-daily";
        }
    }

    @GetMapping("/play/{id}")
    public String playById(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("puzzle", gameplayService.getPlayablePuzzleById(id));
            return "play";
        } catch (PuzzleNotYetAvailableException ex) {
            model.addAttribute("message", ex.getMessage());
            return "no-daily";
        }
    }

    @GetMapping("/archive")
    public String archive(Model model) {
        model.addAttribute("puzzles", gameplayService.getArchive());
        return "archive";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("leaders", statsService.leaderboard());
        return "leaderboard";
    }

    @GetMapping("/stats")
    public String stats(Model model, Principal principal) {
        userRepository.findByUsername(principal.getName())
            .flatMap(statsRepository::findByUser)
            .ifPresent(stats -> model.addAttribute("stats", statsService.statsDto(stats)));
        return "stats";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("form", new AuthController.RegisterForm("", "", ""));
        return "register";
    }

}
