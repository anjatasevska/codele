package mk.ukim.finki.codeleapp.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "puzzles", uniqueConstraints = {
    @UniqueConstraint(name = "uk_puzzles_scheduled_date", columnNames = "scheduled_date")
})
public class Puzzle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    private String language;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String codeContent;

    private String shortClue;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private LocalDate scheduledDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "puzzle_tags", joinColumns = @JoinColumn(name = "puzzle_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
}
