package io.github.alianos.normaliser;

import io.github.alianos.normaliser.scoring.ScoringStrategy;
import io.github.alianos.normaliser.scoring.WordMatchStrategy;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Maps a free-text job title to the best-matching of a fixed set of target titles.
 */
public class Normaliser {

    private static final List<String> DEFAULT_TARGET_TITLES =
            List.of("Architect", "Software engineer", "Quantity surveyor", "Accountant");

    private final List<String> targetTitles;
    private final ScoringStrategy strategy;

    /** Creates a normaliser for the default target titles, scored by {@link WordMatchStrategy}. */
    public Normaliser() {
        this(DEFAULT_TARGET_TITLES, new WordMatchStrategy());
    }

    /**
     * Creates a normaliser for the given target titles, scored by the given strategy.
     * On a tie, the target that comes first in the list wins.
     *
     * @throws NullPointerException if the list, any target or the strategy is null
     * @throws IllegalArgumentException if the list is empty or any target is blank
     */
    public Normaliser(List<String> targetTitles, ScoringStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
        this.targetTitles = List.copyOf(targetTitles);
        if (this.targetTitles.isEmpty()) {
            throw new IllegalArgumentException("targetTitles must not be empty");
        }
        if (this.targetTitles.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("targetTitles must not contain a blank title: " + this.targetTitles);
        }
    }

    /**
     * Returns the target title with the highest quality score q for the input,
     * or "" when no target scores above 0.0. Null or blank input returns "" without
     * consulting the strategy.
     *
     * @throws IllegalStateException if the strategy returns a q outside [0.0, 1.0]
     */
    public String normalise(String inputTitle) {
        if (inputTitle == null || inputTitle.isBlank()) {
            return "";
        }
        return targetTitles.stream()
                .map(targetTitle -> new ScoredTitle(targetTitle, score(inputTitle, targetTitle)))
                .filter(scored -> scored.q() > 0.0)
                .max(Comparator.comparingDouble(ScoredTitle::q))
                .map(ScoredTitle::title)
                .orElse("");
    }

    private double score(String inputTitle, String targetTitle) {
        double q = strategy.score(inputTitle, targetTitle);
        // Written as a negated range check so NaN fails it too.
        if (!(q >= 0.0 && q <= 1.0)) {
            throw new IllegalStateException(
                    "Strategy returned q=" + q + " for target \"" + targetTitle + "\"; expected [0.0, 1.0]");
        }
        return q;
    }

    private record ScoredTitle(String title, double q) {}
}
