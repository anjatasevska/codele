package mk.ukim.finki.codeleapp.service;

import mk.ukim.finki.codeleapp.domain.AppUser;
import mk.ukim.finki.codeleapp.domain.UserRole;
import mk.ukim.finki.codeleapp.domain.UserStats;
import mk.ukim.finki.codeleapp.repository.AppUserRepository;
import mk.ukim.finki.codeleapp.repository.UserStatsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final UserStatsRepository statsRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository userRepository, UserStatsRepository statsRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already used.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already used.");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        user = userRepository.save(user);

        UserStats stats = new UserStats();
        stats.setUser(user);
        statsRepository.save(stats);
    }
}
