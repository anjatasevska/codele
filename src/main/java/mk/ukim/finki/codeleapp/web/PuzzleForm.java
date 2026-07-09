package mk.ukim.finki.codeleapp.web;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import mk.ukim.finki.codeleapp.domain.Category;
import mk.ukim.finki.codeleapp.domain.Difficulty;
import mk.ukim.finki.codeleapp.web.dto.PuzzleRequest;

public class PuzzleForm {
    private String slug;
    private String answer;
    private String language;
    private Category category;
    private Difficulty difficulty;
    private String codeContent;
    private String shortClue;
    private String explanation;
    private LocalDate scheduledDate;
    private String tags;

    public PuzzleRequest toRequest() {
        List<String> tagList = tags == null || tags.isBlank()
            ? List.of()
            : Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new PuzzleRequest(
            slug,
            answer,
            language,
            category,
            difficulty,
            codeContent,
            shortClue,
            explanation,
            scheduledDate,
            tagList
        );
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getCodeContent() {
        return codeContent;
    }

    public void setCodeContent(String codeContent) {
        this.codeContent = codeContent;
    }

    public String getShortClue() {
        return shortClue;
    }

    public void setShortClue(String shortClue) {
        this.shortClue = shortClue;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
