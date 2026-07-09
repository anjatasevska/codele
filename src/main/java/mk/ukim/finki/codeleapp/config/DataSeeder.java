package mk.ukim.finki.codeleapp.config;

import mk.ukim.finki.codeleapp.service.PuzzleSeedService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    @ConditionalOnProperty(name = "codele.seed.enabled", havingValue = "true")
    CommandLineRunner seedPuzzles(PuzzleSeedService puzzleSeedService) {
        return args -> puzzleSeedService.ensureDefaultPuzzles();
    }
}
