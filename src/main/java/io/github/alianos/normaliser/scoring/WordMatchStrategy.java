package io.github.alianos.normaliser.scoring;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Scores q as the fraction of the target's words that appear in the input, ignoring case.
 * Anything other than a letter or digit separates words.
 */
public class WordMatchStrategy implements ScoringStrategy {

    @Override
    public double score(String inputTitle, String targetTitle) {
        Set<String> inputWords = Set.copyOf(words(inputTitle));
        List<String> targetWords = words(targetTitle);
        if (targetWords.isEmpty()) {
            return 0.0;
        }
        return (double) targetWords.stream().filter(inputWords::contains).count() / targetWords.size();
    }

    private static List<String> words(String title) {
        // Splits on each run of characters that are not a Unicode letter (\p{L}) or digit (\p{N}),
        // so "Engineer, Software" gives "engineer" and "software". A leading separator, as in
        // "(London) Accountant", yields an empty first token, which the filter drops.
        return Arrays.stream(title.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(word -> !word.isEmpty())
                .toList();
    }
}
