package io.github.alianos.normaliser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Maps a free-text job title to the best-matching of a fixed set of target titles.
 */
public class Normaliser {

    private static final List<String> DEFAULT_TARGETS =
            List.of("Architect", "Software engineer", "Quantity surveyor", "Accountant");

    private final List<String> targetTitles;

    /** Creates a normaliser for the default target titles. */
    public Normaliser() {
        this.targetTitles = DEFAULT_TARGETS;
    }

    /**
     * Returns the target title with the highest quality score q for the input,
     * or "" when no target scores above 0.0.
     */
    public String normalise(String input) {
        return targetTitles.stream()
                .map(target -> new ScoredTitle(target, score(input, target)))
                .filter(scored -> scored.q() > 0.0)
                .max(Comparator.comparingDouble(ScoredTitle::q))
                .map(ScoredTitle::title)
                .orElse("");
    }

    private record ScoredTitle(String title, double q) {}

    private static double score(String input, String target) {
        Set<String> inputWords = Set.copyOf(Arrays.asList(words(input)));
        String[] targetWords = words(target);
        long matched = 0;
        for (String word : targetWords) {
            if (inputWords.contains(word)) {
                matched++;
            }
        }
        return (double) matched / targetWords.length;
    }

    private static String[] words(String title) {
        return title.toLowerCase(Locale.ROOT).trim().split("\\s+");
    }
}
