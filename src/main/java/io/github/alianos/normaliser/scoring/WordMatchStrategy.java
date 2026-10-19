package io.github.alianos.normaliser.scoring;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * Scores q as the fraction of the target's words that appear in the input, ignoring case.
 */
public class WordMatchStrategy implements ScoringStrategy {

    @Override
    public double score(String inputTitle, String targetTitle) {
        Set<String> inputWords = Set.copyOf(Arrays.asList(words(inputTitle)));
        String[] targetWords = words(targetTitle);
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
