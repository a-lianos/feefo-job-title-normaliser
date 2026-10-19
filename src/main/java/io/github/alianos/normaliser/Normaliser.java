package io.github.alianos.normaliser;

import io.github.alianos.normaliser.scoring.ScoringStrategy;
import io.github.alianos.normaliser.scoring.WordMatchStrategy;
import java.util.Comparator;
import java.util.List;

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
     */
    public Normaliser(List<String> targetTitles, ScoringStrategy strategy) {
        this.targetTitles = List.copyOf(targetTitles);
        this.strategy = strategy;
    }

    /**
     * Returns the target title with the highest quality score q for the input,
     * or "" when no target scores above 0.0.
     *
     * @throws IllegalStateException if the strategy returns a q outside [0.0, 1.0]
     */
    public String normalise(String input) {
        return targetTitles.stream()
                .map(target -> new ScoredTitle(target, score(input, target)))
                .filter(scored -> scored.q() > 0.0)
                .max(Comparator.comparingDouble(ScoredTitle::q))
                .map(ScoredTitle::title)
                .orElse("");
    }

    private double score(String input, String target) {
        double q = strategy.score(input, target);
        // Written as a negated range check so NaN fails it too.
        if (!(q >= 0.0 && q <= 1.0)) {
            throw new IllegalStateException(
                    "Strategy returned q=" + q + " for target \"" + target + "\"; expected [0.0, 1.0]");
        }
        return q;
    }

    private record ScoredTitle(String title, double q) {}
}
