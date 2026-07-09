package mk.ukim.finki.codeleapp.config;

import mk.ukim.finki.codeleapp.domain.AppUser;
import mk.ukim.finki.codeleapp.domain.UserRole;
import mk.ukim.finki.codeleapp.domain.UserStats;
import mk.ukim.finki.codeleapp.repository.AppUserRepository;
import mk.ukim.finki.codeleapp.repository.UserStatsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserSeeder {

    @Bean
    CommandLineRunner seedAdminUser(
        AppUserRepository userRepository,
        UserStatsRepository statsRepository,
        PasswordEncoder passwordEncoder,
        @Value("${codele.admin.username:admin}") String adminUsername,
        @Value("${codele.admin.email:admin@codele.local}") String adminEmail,
        @Value("${codele.admin.password:changeme}") String adminPassword,
        @Value("${codele.admin.sync-password-on-startup:false}") boolean syncPasswordOnStartup
    ) {
        return args -> {
            userRepository.findByUsername(adminUsername).ifPresentOrElse(user -> {
                if (user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                }
                if (syncPasswordOnStartup) {
                    user.setPasswordHash(passwordEncoder.encode(adminPassword));
                }
                userRepository.save(user);
            }, () -> {
                AppUser admin = new AppUser();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.ADMIN);
                admin = userRepository.save(admin);

                UserStats stats = new UserStats();
                stats.setUser(admin);
                statsRepository.save(stats);
            });
        };
    }
}
