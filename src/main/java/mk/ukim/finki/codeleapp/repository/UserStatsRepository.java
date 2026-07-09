package mk.ukim.finki.codeleapp.repository;

import java.util.Optional;
import mk.ukim.finki.codeleapp.domain.AppUser;
import mk.ukim.finki.codeleapp.domain.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    Optional<UserStats> findByUser(AppUser user);
}
